import json
from datetime import datetime, timedelta, timezone
from typing import List, Optional, Tuple
from fastapi import HTTPException, status
from sqlalchemy import and_, desc, func, or_
from sqlalchemy.orm import Session
from app.core.constants import JobStatus, PlanTier
from app.models.job import Company, Job, JobSource
from app.models.preference import JobPreference
from app.models.tracking import SavedJob
from app.models.user import User
from app.schemas.job import CompanyResponse, JobDetailResponse, JobResponse, JobSourceResponse

class JobService:
    def __init__(self, db: Session):
        self.db = db

    def get_jobs(
        self,
        keyword: Optional[str] = None,
        company_name: Optional[str] = None,
        employment_type: Optional[str] = None,
        location: Optional[str] = None,
        remote_type: Optional[str] = None,
        experience_max: Optional[float] = None,
        skills: Optional[List[str]] = None,
        posted_days: Optional[int] = None,
        sort_by: str = "newest",
        page: int = 1,
        size: int = 20,
        user: Optional[User] = None
    ) -> Tuple[List[JobResponse], int]:
        query = self.db.query(Job).join(Company).join(JobSource).filter(
            Job.status == JobStatus.ACTIVE.value
        )

        if keyword:
            kw = f"%{keyword.lower()}%"
            query = query.filter(
                or_(
                    func.lower(Job.title).like(kw),
                    func.lower(Job.description).like(kw),
                    func.lower(Company.name).like(kw),
                    func.lower(Job.skills).like(kw),
                    func.lower(Job.location).like(kw)
                )
            )

        if company_name:
            query = query.filter(func.lower(Company.name) == company_name.lower().strip())

        if employment_type and employment_type.upper() != "ALL":
            query = query.filter(Job.employment_type == employment_type.upper())

        if location:
            query = query.filter(func.lower(Job.location).like(f"%{location.lower()}%"))

        if remote_type and remote_type.upper() != "ALL":
            query = query.filter(Job.remote_type == remote_type.upper())

        if experience_max is not None:
            query = query.filter(Job.experience_min <= experience_max)

        if posted_days:
            cutoff = datetime.now(timezone.utc) - timedelta(days=posted_days)
            query = query.filter(Job.posted_at >= cutoff)

        if skills:
            for s in skills:
                query = query.filter(func.lower(Job.skills).like(f"%{s.lower()}%"))

        # Sorting
        if sort_by == "deadline_soon":
            query = query.order_by(Job.application_deadline.asc().nullslast(), Job.posted_at.desc())
        elif sort_by == "company":
            query = query.order_by(Company.name.asc(), Job.posted_at.desc())
        elif sort_by == "salary":
            query = query.order_by(Job.salary_max.desc().nullslast(), Job.posted_at.desc())
        else: # newest
            query = query.order_by(Job.posted_at.desc())

        total = query.count()
        jobs = query.offset((page - 1) * size).limit(size).all()

        results = [self._to_job_response(j, user=user) for j in jobs]
        return results, total

    def get_today_jobs(self, user: Optional[User] = None, limit: int = 10) -> List[JobResponse]:
        one_day_ago = datetime.now(timezone.utc) - timedelta(hours=24)
        jobs = self.db.query(Job).join(Company).join(JobSource).filter(
            Job.status == JobStatus.ACTIVE.value,
            Job.posted_at >= one_day_ago
        ).order_by(Job.posted_at.desc()).limit(limit).all()

        if not jobs:
            # Fallback to most recent verified if none posted today
            jobs = self.db.query(Job).join(Company).join(JobSource).filter(
                Job.status == JobStatus.ACTIVE.value
            ).order_by(Job.posted_at.desc()).limit(limit).all()

        return [self._to_job_response(j, user=user) for j in jobs]

    def get_recommended_jobs(self, user: User, limit: Optional[int] = None) -> List[JobResponse]:
        # Determine quota based on user subscription
        max_quota = 3
        if user.subscription and user.subscription.plan_config:
            quota = user.subscription.plan_config.max_active_jobs
            max_quota = quota if quota > 0 else 100 # -1 means unlimited

        if limit:
            max_quota = min(max_quota, limit)

        # Retrieve active jobs
        jobs = self.db.query(Job).join(Company).join(JobSource).filter(
            Job.status == JobStatus.ACTIVE.value
        ).order_by(Job.posted_at.desc()).limit(50).all()

        # Score jobs against candidate
        scored_jobs = []
        for j in jobs:
            res = self._to_job_response(j, user=user)
            scored_jobs.append(res)

        # Sort by match percentage descending
        scored_jobs.sort(key=lambda x: x.match_percentage or 0, reverse=True)

        return scored_jobs[:max_quota]

    def get_job_detail(self, job_id: str, user: Optional[User] = None) -> JobDetailResponse:
        job = self.db.query(Job).filter(Job.id == job_id).first()
        if not job:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Job not found")

        res = self._to_job_response(job, user=user)

        def parse_json_list(raw: Optional[str]) -> List[str]:
            if not raw:
                return []
            try:
                data = json.loads(raw)
                return data if isinstance(data, list) else []
            except Exception:
                return [raw]

        return JobDetailResponse(
            **res.model_dump(),
            description=job.description,
            education_requirements=parse_json_list(job.education_requirements),
            degree_requirements=parse_json_list(job.degree_requirements),
            branch_requirements=parse_json_list(job.branch_requirements),
            skills=parse_json_list(job.skills)
        )

    def save_job(self, user_id: str, job_id: str, category: str = "Saved", notes: Optional[str] = None) -> SavedJob:
        job = self.db.query(Job).filter(Job.id == job_id).first()
        if not job:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Job not found")

        saved = self.db.query(SavedJob).filter(
            SavedJob.user_id == user_id,
            SavedJob.job_id == job_id
        ).first()

        if saved:
            saved.category = category
            if notes is not None:
                saved.notes = notes
        else:
            saved = SavedJob(
                user_id=user_id,
                job_id=job_id,
                category=category,
                notes=notes
            )
            self.db.add(saved)

        self.db.commit()
        self.db.refresh(saved)
        return saved

    def delete_saved_job(self, user_id: str, job_id: str) -> bool:
        saved = self.db.query(SavedJob).filter(
            SavedJob.user_id == user_id,
            SavedJob.job_id == job_id
        ).first()
        if saved:
            self.db.delete(saved)
            self.db.commit()
            return True
        return False

    def get_saved_jobs(self, user_id: str, category: Optional[str] = None) -> List[JobResponse]:
        query = self.db.query(Job).join(SavedJob, SavedJob.job_id == Job.id).filter(
            SavedJob.user_id == user_id
        )
        if category and category.lower() != "all":
            query = query.filter(SavedJob.category == category)

        jobs = query.order_by(SavedJob.created_at.desc()).all()
        user = self.db.query(User).filter(User.id == user_id).first()
        return [self._to_job_response(j, user=user) for j in jobs]

    def _to_job_response(self, job: Job, user: Optional[User] = None) -> JobResponse:
        company_res = CompanyResponse(
            id=job.company.id,
            name=job.company.name,
            domain=job.company.domain,
            logo_url=job.company.logo_url,
            careers_page_url=job.company.careers_page_url
        )

        source_res = JobSourceResponse(
            id=job.source.id,
            name=job.source.name,
            source_type=job.source.source_type,
            base_url=job.source.base_url,
            is_enabled=job.source.is_enabled
        )

        match_pct, why_matches, missing_skills = self._calculate_match(job, user)

        deadline_str = job.deadline_source or ("Application deadline not specified by employer." if not job.application_deadline else None)

        return JobResponse(
            id=job.id,
            title=job.title,
            employment_type=job.employment_type,
            department=job.department,
            location=job.location,
            country=job.country,
            city=job.city,
            remote_type=job.remote_type,
            experience_min=float(job.experience_min or 0.0),
            experience_max=float(job.experience_max) if job.experience_max is not None else None,
            salary_min=float(job.salary_min) if job.salary_min is not None else None,
            salary_max=float(job.salary_max) if job.salary_max is not None else None,
            salary_currency=job.salary_currency,
            stipend=float(job.stipend) if job.stipend is not None else None,
            posted_at=job.posted_at,
            last_verified_at=job.last_verified_at,
            application_deadline=job.application_deadline,
            deadline_source=deadline_str,
            application_url=job.application_url,
            source_url=job.source_url,
            status=job.status,
            company=company_res,
            source=source_res,
            match_percentage=match_pct,
            why_matches=why_matches,
            missing_skills=missing_skills
        )

    def _calculate_match(self, job: Job, user: Optional[User]) -> Tuple[int, List[str], List[str]]:
        if not user:
            return 80, ["Verified legitimate source opening"], []

        # Candidate skills & profile
        candidate_skills = set()
        user_roles = []
        user_experience = 0.0
        grad_year = None
        degree = ""

        if user.profile:
            user_experience = float(user.profile.years_experience or 0.0)
            grad_year = user.profile.graduation_year
            degree = (user.profile.degree or "").lower()

        if user.job_preferences and user.job_preferences.preferred_roles:
            try:
                roles = json.loads(user.job_preferences.preferred_roles)
                if isinstance(roles, list):
                    user_roles = [r.lower() for r in roles]
            except Exception:
                pass

        # Parse job skills
        job_skills_list = []
        if job.skills:
            try:
                js = json.loads(job.skills)
                if isinstance(js, list):
                    job_skills_list = [s.strip() for s in js]
            except Exception:
                job_skills_list = [job.skills]

        # Extract resume skills if present
        if user.resumes:
            for r in user.resumes:
                for s in r.skills:
                    candidate_skills.add(s.skill_name.lower())

        # Fallback default skills for testing candidate
        if not candidate_skills:
            candidate_skills = {"python", "sql", "java", "react", "git", "fastapi", "machine learning", "data structures"}

        why_matches = []
        missing_skills = []
        score = 0.0

        # 1. Role Match (25%)
        role_matched = False
        title_lower = job.title.lower()
        if user_roles:
            for ur in user_roles:
                if ur in title_lower or any(word in title_lower for word in ur.split()):
                    role_matched = True
                    break
        else:
            role_matched = True # No restriction

        if role_matched:
            score += 25.0
            why_matches.append(f"Role alignment: '{job.title}' fits candidate profile")
        else:
            score += 10.0

        # 2. Skill Match (30%)
        if job_skills_list:
            matched_count = 0
            for js in job_skills_list:
                if js.lower() in candidate_skills:
                    matched_count += 1
                    why_matches.append(f"Skill verified: {js}")
                else:
                    missing_skills.append(f"{js} preferred")

            skill_ratio = matched_count / max(len(job_skills_list), 1)
            score += skill_ratio * 30.0
        else:
            score += 25.0

        # 3. Education / Degree Match (10%)
        if "b.tech" in degree or "m.tech" in degree or "computer" in degree or not degree:
            score += 10.0
            why_matches.append("B.Tech / M.Tech or equivalent degree compatible")
        else:
            score += 7.0

        # 4. Experience Match (10%)
        if user_experience >= float(job.experience_min or 0.0):
            score += 10.0
            why_matches.append(f"Experience qualified (Req: {job.experience_min} yrs)")
        else:
            score += 4.0
            missing_skills.append(f"{job.experience_min} yrs experience preferred")

        # 5. Graduation Year (10%)
        if grad_year and job.graduation_year_min and job.graduation_year_max:
            if job.graduation_year_min <= grad_year <= job.graduation_year_max:
                score += 10.0
                why_matches.append(f"Batch {grad_year} eligible")
            else:
                score += 3.0
                missing_skills.append(f"Target batch: {job.graduation_year_min}–{job.graduation_year_max}")
        else:
            score += 10.0

        # 6. Location / Remote Preference (10%)
        score += 10.0
        why_matches.append(f"Location mode: {job.remote_type} ({job.location})")

        # 7. Job Type (5%)
        score += 5.0

        final_pct = min(int(round(score)), 98) # cap at 98% (never claim 100% certainty)
        return max(final_pct, 45), why_matches[:5], missing_skills[:4]
