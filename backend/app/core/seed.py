import json
from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from app.core.constants import JobStatus, PlanTier, RemoteType, SourceType
from app.models.job import Company, Job, JobEligibility, JobLocation, JobSkill, JobSource, SourceHealth
from app.models.subscription import SubscriptionPlanConfig

def seed_database(db: Session):
    # 1. Seed Subscription Plans
    default_plans = [
        {
            "plan_tier": PlanTier.FREE.value,
            "name": "Free Starter",
            "price": 0.00,
            "currency": "INR",
            "billing_cycle": "MONTHLY",
            "max_active_jobs": 3,
            "refresh_interval_days": 3,
            "ai_features_enabled": False,
            "expanded_alerts_enabled": False,
            "is_active": True
        },
        {
            "plan_tier": PlanTier.PRO_99.value,
            "name": "Pro Career",
            "price": 99.00,
            "currency": "INR",
            "billing_cycle": "MONTHLY",
            "max_active_jobs": 20,
            "refresh_interval_days": 3,
            "ai_features_enabled": False,
            "expanded_alerts_enabled": True,
            "is_active": True
        },
        {
            "plan_tier": PlanTier.ELITE_189.value,
            "name": "Elite Discovery",
            "price": 189.00,
            "currency": "INR",
            "billing_cycle": "MONTHLY",
            "max_active_jobs": -1, # Unlimited
            "refresh_interval_days": 1,
            "ai_features_enabled": True,
            "expanded_alerts_enabled": True,
            "is_active": True
        },
        {
            "plan_tier": PlanTier.PREMIUM_CONFIGURABLE.value,
            "name": "Executive Pass",
            "price": 349.00,
            "currency": "INR",
            "billing_cycle": "MONTHLY",
            "max_active_jobs": -1,
            "refresh_interval_days": 1,
            "ai_features_enabled": True,
            "expanded_alerts_enabled": True,
            "is_active": True
        }
    ]

    for p in default_plans:
        if not db.query(SubscriptionPlanConfig).filter(SubscriptionPlanConfig.plan_tier == p["plan_tier"]).first():
            db.add(SubscriptionPlanConfig(**p))
    db.commit()

    # 2. Seed Sources
    sources_data = [
        {
            "name": "Amazon Careers (Official Company Site)",
            "source_type": SourceType.OFFICIAL_COMPANY.value,
            "base_url": "https://www.amazon.jobs",
            "api_endpoint": "https://www.amazon.jobs/en/search.json",
            "is_enabled": True,
            "priority": 1,
            "refresh_interval_minutes": 30
        },
        {
            "name": "Stripe (Greenhouse ATS)",
            "source_type": SourceType.OFFICIAL_ATS.value,
            "base_url": "https://boards.greenhouse.io/stripe",
            "api_endpoint": "https://boards-api.greenhouse.io/v1/boards/stripe/jobs",
            "is_enabled": True,
            "priority": 1,
            "refresh_interval_minutes": 30
        },
        {
            "name": "Razorpay Careers (Lever ATS)",
            "source_type": SourceType.OFFICIAL_ATS.value,
            "base_url": "https://jobs.lever.co/razorpay",
            "api_endpoint": "https://api.lever.co/v0/postings/razorpay",
            "is_enabled": True,
            "priority": 1,
            "refresh_interval_minutes": 30
        },
        {
            "name": "Google Careers (Official Company Site)",
            "source_type": SourceType.OFFICIAL_COMPANY.value,
            "base_url": "https://careers.google.com",
            "api_endpoint": "https://careers.google.com/api/v3/search",
            "is_enabled": True,
            "priority": 1,
            "refresh_interval_minutes": 60
        }
    ]

    source_map = {}
    for s_info in sources_data:
        src = db.query(JobSource).filter(JobSource.name == s_info["name"]).first()
        if not src:
            src = JobSource(**s_info)
            db.add(src)
            db.flush()
            # Add healthy status
            health = SourceHealth(
                source_id=src.id,
                status="HEALTHY",
                response_time_ms=124,
                jobs_ingested_count=12
            )
            db.add(health)
        source_map[s_info["name"]] = src
    db.commit()

    # 3. Seed Companies
    companies_data = [
        {"name": "Amazon", "normalized_name": "amazon", "domain": "amazon.com", "careers_page_url": "https://amazon.jobs"},
        {"name": "Google", "normalized_name": "google", "domain": "google.com", "careers_page_url": "https://careers.google.com"},
        {"name": "Stripe", "normalized_name": "stripe", "domain": "stripe.com", "careers_page_url": "https://stripe.com/jobs"},
        {"name": "Razorpay", "normalized_name": "razorpay", "domain": "razorpay.com", "careers_page_url": "https://razorpay.com/jobs"},
        {"name": "Microsoft", "normalized_name": "microsoft", "domain": "microsoft.com", "careers_page_url": "https://careers.microsoft.com"},
        {"name": "Swiggy", "normalized_name": "swiggy", "domain": "swiggy.com", "careers_page_url": "https://careers.swiggy.com"}
    ]

    company_map = {}
    for c_info in companies_data:
        comp = db.query(Company).filter(Company.name == c_info["name"]).first()
        if not comp:
            comp = Company(**c_info)
            db.add(comp)
            db.flush()
        company_map[c_info["name"]] = comp
    db.commit()

    # 4. Seed Verified Active Opportunities
    now = datetime.now(timezone.utc)
    jobs_seed = [
        {
            "company": "Amazon",
            "source": "Amazon Careers (Official Company Site)",
            "external_job_id": "AMZN-SDE-2026-01",
            "title": "Software Development Engineer Intern - 2026",
            "description": "Amazon is seeking ambitious Software Development Engineer Interns to join our engineering teams in Bengaluru and Hyderabad. You will work on massive-scale distributed systems, build customer-facing web services, and collaborate with senior engineers across AWS and Retail.",
            "employment_type": "INTERNSHIP",
            "job_category": "Software Engineering",
            "department": "Core Platform",
            "location": "Bengaluru, Karnataka",
            "city": "Bengaluru",
            "remote_type": RemoteType.HYBRID.value,
            "experience_min": 0.0,
            "experience_max": 1.0,
            "skills": ["Java", "Data Structures", "Algorithms", "Python", "SQL", "Git"],
            "education_requirements": ["B.Tech", "B.E.", "M.Tech", "MCA"],
            "degree_requirements": ["Computer Science", "Information Technology", "Electronics"],
            "graduation_year_min": 2026,
            "graduation_year_max": 2027,
            "stipend": 80000.0,
            "posted_at": now - timedelta(hours=4),
            "application_deadline": now + timedelta(days=25),
            "deadline_source": "Official employer posting declares deadline 25 days remaining.",
            "application_url": "https://www.amazon.jobs/en/jobs/AMZN-SDE-2026-01"
        },
        {
            "company": "Stripe",
            "source": "Stripe (Greenhouse ATS)",
            "external_job_id": "STRIPE-SWE-INFRA-02",
            "title": "Software Engineer, Core Infrastructure",
            "description": "Stripe powers online commerce for millions of global businesses. As an Infrastructure Software Engineer, you will design, scale, and operate reliable financial ledger services handling billions in payments with zero downtime.",
            "employment_type": "FULL_TIME",
            "job_category": "Backend Engineering",
            "department": "Payments Infrastructure",
            "location": "Bengaluru, Karnataka",
            "city": "Bengaluru",
            "remote_type": RemoteType.REMOTE.value,
            "experience_min": 0.5,
            "experience_max": 3.0,
            "skills": ["Python", "Go", "Distributed Systems", "SQL", "Docker", "Linux"],
            "education_requirements": ["B.Tech", "B.E.", "M.Tech", "Equivalent"],
            "degree_requirements": ["Computer Science or relevant quantitative field"],
            "graduation_year_min": 2023,
            "graduation_year_max": 2026,
            "salary_min": 1800000.0,
            "salary_max": 2800000.0,
            "posted_at": now - timedelta(hours=10),
            "application_deadline": None,
            "deadline_source": "Application deadline not specified by employer.",
            "application_url": "https://boards.greenhouse.io/stripe/jobs/STRIPE-SWE-INFRA-02"
        },
        {
            "company": "Razorpay",
            "source": "Razorpay Careers (Lever ATS)",
            "external_job_id": "RZP-DS-INTERN-03",
            "title": "Data Science & ML Intern",
            "description": "Razorpay's Risk and Fraud Detection ML team is looking for a Data Science Intern. You will develop machine learning models to detect fraudulent transactions in real-time, analyze transaction graph networks, and deploy inference pipelines.",
            "employment_type": "INTERNSHIP",
            "job_category": "Data Science & AI",
            "department": "Risk & Fraud Intelligence",
            "location": "Bengaluru, Karnataka",
            "city": "Bengaluru",
            "remote_type": RemoteType.HYBRID.value,
            "experience_min": 0.0,
            "experience_max": 1.0,
            "skills": ["Python", "Machine Learning", "Pandas", "SQL", "Scikit-Learn", "Git"],
            "education_requirements": ["B.Tech", "M.Tech", "M.Sc Data Science"],
            "degree_requirements": ["Computer Science", "Data Science", "Statistics", "Mathematics"],
            "graduation_year_min": 2025,
            "graduation_year_max": 2026,
            "stipend": 50000.0,
            "posted_at": now - timedelta(hours=18),
            "application_deadline": now + timedelta(days=14),
            "deadline_source": "Official employer portal declares deadline in 14 days.",
            "application_url": "https://jobs.lever.co/razorpay/RZP-DS-INTERN-03"
        },
        {
            "company": "Google",
            "source": "Google Careers (Official Company Site)",
            "external_job_id": "GOOG-STEP-2026-04",
            "title": "Student Researcher / STEP Intern - Summer 2026",
            "description": "Google's STEP program is a developmental internship for undergraduate students passionate about Computer Science. Interns work in pairs alongside Google software engineers on production codebases.",
            "employment_type": "INTERNSHIP",
            "job_category": "Software Development",
            "department": "Google Cloud & Search",
            "location": "Hyderabad, Telangana",
            "city": "Hyderabad",
            "remote_type": RemoteType.ON_SITE.value,
            "experience_min": 0.0,
            "experience_max": 0.5,
            "skills": ["C++", "Python", "Data Structures", "Problem Solving", "Linux"],
            "education_requirements": ["Bachelor's in progress"],
            "degree_requirements": ["Computer Science", "Electrical Engineering", "Related field"],
            "graduation_year_min": 2026,
            "graduation_year_max": 2027,
            "stipend": 90000.0,
            "posted_at": now - timedelta(days=1),
            "application_deadline": now + timedelta(days=20),
            "deadline_source": "Official Google Careers specifies deadline 20 days remaining.",
            "application_url": "https://careers.google.com/jobs/results/GOOG-STEP-2026-04"
        },
        {
            "company": "Swiggy",
            "source": "Amazon Careers (Official Company Site)",
            "external_job_id": "SWIG-FS-2026-05",
            "title": "Full Stack Engineer I",
            "description": "Join Swiggy's Delivery Fleet engineering group. You will build high-throughput microservices in Go and Python, responsive web workflows in React and TypeScript, and optimize order dispatch latency.",
            "employment_type": "FULL_TIME",
            "job_category": "Full Stack Development",
            "department": "Logistics & Marketplace",
            "location": "Bengaluru, Karnataka",
            "city": "Bengaluru",
            "remote_type": RemoteType.HYBRID.value,
            "experience_min": 0.5,
            "experience_max": 2.5,
            "skills": ["React", "TypeScript", "Python", "FastAPI", "PostgreSQL", "Kafka"],
            "education_requirements": ["B.Tech", "B.E.", "MCA"],
            "degree_requirements": ["Any engineering or computer applications discipline"],
            "graduation_year_min": 2024,
            "graduation_year_max": 2026,
            "salary_min": 1400000.0,
            "salary_max": 2200000.0,
            "posted_at": now - timedelta(days=2),
            "application_deadline": None,
            "deadline_source": "Application deadline not specified by employer.",
            "application_url": "https://careers.swiggy.com/jobs/SWIG-FS-2026-05"
        }
    ]

    for j_data in jobs_seed:
        company_obj = company_map[j_data["company"]]
        source_obj = source_map[j_data["source"]]

        existing = db.query(Job).filter(
            Job.source_id == source_obj.id,
            Job.external_job_id == j_data["external_job_id"]
        ).first()

        if not existing:
            job = Job(
                source_id=source_obj.id,
                company_id=company_obj.id,
                external_job_id=j_data["external_job_id"],
                title=j_data["title"],
                normalized_title=j_data["title"].strip(),
                description=j_data["description"],
                normalized_description=j_data["description"],
                employment_type=j_data["employment_type"],
                job_category=j_data["job_category"],
                department=j_data["department"],
                location=j_data["location"],
                city=j_data["city"],
                remote_type=j_data["remote_type"],
                experience_min=j_data["experience_min"],
                experience_max=j_data["experience_max"],
                skills=json.dumps(j_data["skills"]),
                education_requirements=json.dumps(j_data["education_requirements"]),
                degree_requirements=json.dumps(j_data["degree_requirements"]),
                graduation_year_min=j_data["graduation_year_min"],
                graduation_year_max=j_data["graduation_year_max"],
                salary_min=j_data.get("salary_min"),
                salary_max=j_data.get("salary_max"),
                stipend=j_data.get("stipend"),
                posted_at=j_data["posted_at"],
                last_verified_at=now,
                application_deadline=j_data["application_deadline"],
                deadline_source=j_data["deadline_source"],
                application_url=j_data["application_url"],
                source_url=j_data["application_url"],
                status=JobStatus.ACTIVE.value
            )
            db.add(job)
            db.flush()

            # Add skills relationships
            for s in j_data["skills"]:
                db.add(JobSkill(job_id=job.id, skill_name=s, is_required=True))

            # Add location
            db.add(JobLocation(job_id=job.id, city=j_data["city"], state="Karnataka", country="India", is_primary=True))

            # Add eligibility
            db.add(JobEligibility(
                job_id=job.id,
                criteria_summary=f"Eligible for {', '.join(j_data['education_requirements'])}, batches {j_data['graduation_year_min']}–{j_data['graduation_year_max']}",
                allowed_branches=json.dumps(j_data["degree_requirements"]),
                allowed_batch_years=json.dumps([j_data["graduation_year_min"], j_data["graduation_year_max"]])
            ))

    db.commit()
