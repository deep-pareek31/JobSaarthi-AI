from datetime import date, datetime, timezone
from typing import Any, Dict, List, Optional
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.models.job import Job
from app.models.tracking import ApplicationStatusHistory, AppliedJob
from app.models.user import User
from app.schemas.job import CompanyResponse, JobResponse, JobSourceResponse
from app.schemas.tracking import AppliedJobCreate, AppliedJobResponse, AppliedJobUpdate

class ApplicationService:
    def __init__(self, db: Session):
        self.db = db

    def apply_job(self, user_id: str, payload: AppliedJobCreate) -> AppliedJobResponse:
        job = self.db.query(Job).filter(Job.id == payload.job_id).first()
        if not job:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Job not found")

        applied = self.db.query(AppliedJob).filter(
            AppliedJob.user_id == user_id,
            AppliedJob.job_id == payload.job_id
        ).first()

        if applied:
            applied.status = payload.status
            if payload.notes:
                applied.notes = payload.notes
            applied.applied_date = payload.applied_date
        else:
            applied = AppliedJob(
                user_id=user_id,
                job_id=payload.job_id,
                status=payload.status,
                applied_date=payload.applied_date,
                notes=payload.notes,
                resume_used_id=payload.resume_used_id
            )
            self.db.add(applied)
            self.db.flush()

            # Record status history
            history = ApplicationStatusHistory(
                applied_job_id=applied.id,
                from_status="None",
                to_status=payload.status,
                remarks=payload.notes or "Initial application marked by candidate."
            )
            self.db.add(history)

        self.db.commit()
        self.db.refresh(applied)
        return self._to_response(applied)

    def update_status(self, user_id: str, applied_id: str, payload: AppliedJobUpdate) -> AppliedJobResponse:
        applied = self.db.query(AppliedJob).filter(
            AppliedJob.id == applied_id,
            AppliedJob.user_id == user_id
        ).first()

        if not applied:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Application record not found")

        old_status = applied.status
        applied.status = payload.status
        if payload.notes is not None:
            applied.notes = payload.notes

        history = ApplicationStatusHistory(
            applied_job_id=applied.id,
            from_status=old_status,
            to_status=payload.status,
            remarks=payload.remarks or f"Status transitioned from {old_status} to {payload.status}."
        )
        self.db.add(history)

        self.db.commit()
        self.db.refresh(applied)
        return self._to_response(applied)

    def get_applications(self, user_id: str, status_filter: Optional[str] = None) -> List[AppliedJobResponse]:
        query = self.db.query(AppliedJob).filter(AppliedJob.user_id == user_id)
        if status_filter and status_filter.lower() != "all":
            query = query.filter(AppliedJob.status == status_filter)

        results = query.order_by(AppliedJob.applied_date.desc(), AppliedJob.created_at.desc()).all()
        return [self._to_response(a) for a in results]

    def get_application_stats(self, user_id: str) -> Dict[str, int]:
        apps = self.db.query(AppliedJob).filter(AppliedJob.user_id == user_id).all()
        stats = {
            "total_applied": len(apps),
            "applied_this_month": len(apps),
            "interviews": 0,
            "assessments": 0,
            "offers": 0,
            "rejected": 0,
            "pending": 0
        }
        for a in apps:
            st = a.status.lower()
            if "interview" in st:
                stats["interviews"] += 1
            elif "assessment" in st:
                stats["assessments"] += 1
            elif "offer" in st:
                stats["offers"] += 1
            elif "reject" in st:
                stats["rejected"] += 1
            else:
                stats["pending"] += 1

        return stats

    def _to_response(self, applied: AppliedJob) -> AppliedJobResponse:
        job = applied.job
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

        job_res = JobResponse(
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
            deadline_source=job.deadline_source or ("Application deadline not specified by employer." if not job.application_deadline else None),
            application_url=job.application_url,
            source_url=job.source_url,
            status=job.status,
            company=company_res,
            source=source_res,
            match_percentage=85,
            why_matches=[],
            missing_skills=[]
        )

        return AppliedJobResponse(
            id=applied.id,
            job_id=applied.job_id,
            status=applied.status,
            applied_date=applied.applied_date,
            notes=applied.notes,
            updated_at=applied.updated_at,
            job=job_res
        )
