import re
from datetime import datetime, timedelta, timezone
from typing import Any, Dict, List, Optional, Tuple
from sqlalchemy import func, or_
from sqlalchemy.orm import Session
from app.models.ai import AIConversation
from app.models.job import Company, Job
from app.schemas.job import JobResponse
from app.services.job_service import JobService

class AISearchService:
    def __init__(self, db: Session):
        self.db = db
        self.job_service = JobService(db)

    def parse_natural_language_query(self, prompt: str) -> Dict[str, Any]:
        """
        Translates natural language text into structured database query parameters.
        Rule: AI never invents a job; only translates intent into structured filters.
        """
        text = prompt.lower()
        filters: Dict[str, Any] = {
            "keyword": None,
            "company_name": None,
            "employment_type": None,
            "location": None,
            "remote_type": None,
            "experience_max": None,
            "posted_days": None,
            "skills": []
        }

        # 1. Detect Employment Type
        if "intern" in text:
            filters["employment_type"] = "INTERNSHIP"
        elif "full time" in text or "fulltime" in text:
            filters["employment_type"] = "FULL_TIME"
        elif "contract" in text:
            filters["employment_type"] = "CONTRACT"

        # 2. Detect Remote / Work mode
        if "remote" in text:
            filters["remote_type"] = "REMOTE"
        elif "hybrid" in text:
            filters["remote_type"] = "HYBRID"
        elif "on-site" in text or "onsite" in text:
            filters["remote_type"] = "ON_SITE"

        # 3. Detect Locations
        cities = ["bengaluru", "bangalore", "hyderabad", "pune", "delhi", "noida", "gurugram", "gurgaon", "mumbai", "chennai"]
        for c in cities:
            if c in text:
                filters["location"] = "Bengaluru" if c in ["bengaluru", "bangalore"] else c.capitalize()
                break

        # 4. Detect Companies
        companies = ["amazon", "google", "microsoft", "stripe", "razorpay", "swiggy", "flipkart", "uber", "zomato", "atlassian"]
        for comp in companies:
            if comp in text:
                filters["company_name"] = comp.capitalize()
                break

        # 5. Detect Experience
        if "fresher" in text or "entry level" in text or "0 years" in text:
            filters["experience_max"] = 0.5
        elif "1-2" in text or "1 year" in text:
            filters["experience_max"] = 2.0

        # 6. Detect Posted Date Window
        if "today" in text:
            filters["posted_days"] = 1
        elif "3 days" in text:
            filters["posted_days"] = 3
        elif "7 days" in text or "last week" in text:
            filters["posted_days"] = 7
        elif "30 days" in text or "last month" in text:
            filters["posted_days"] = 30

        # 7. Detect Skills
        known_skills = ["python", "sql", "java", "react", "c++", "go", "golang", "machine learning", "ml", "aws", "docker", "fastapi", "django", "nodejs"]
        for sk in known_skills:
            if re.search(r'\b' + re.escape(sk) + r'\b', text):
                filters["skills"].append(sk)

        # 8. Detect General Role Keyword if not extracted
        if "software" in text or "engineer" in text or "developer" in text:
            filters["keyword"] = "Software"
        elif "data" in text or "analyst" in text:
            filters["keyword"] = "Data"

        return filters

    def search_with_natural_language(
        self,
        user_id: str,
        query_text: str,
        session_id: str = "default_session"
    ) -> Tuple[List[JobResponse], Dict[str, Any], str]:
        filters = self.parse_natural_language_query(query_text)

        # Query database with derived structured filters
        jobs, total = self.job_service.get_jobs(
            keyword=filters.get("keyword"),
            company_name=filters.get("company_name"),
            employment_type=filters.get("employment_type"),
            location=filters.get("location"),
            remote_type=filters.get("remote_type"),
            experience_max=filters.get("experience_max"),
            skills=filters.get("skills") if filters.get("skills") else None,
            posted_days=filters.get("posted_days"),
            size=15
        )

        # Build natural language response explaining filters
        clauses = []
        if filters.get("company_name"):
            clauses.append(f"at {filters['company_name']}")
        if filters.get("employment_type"):
            clauses.append(f"for {filters['employment_type'].lower()} roles")
        if filters.get("location"):
            clauses.append(f"in {filters['location']}")
        if filters.get("remote_type"):
            clauses.append(f"with {filters['remote_type'].lower()} work mode")
        if filters.get("skills"):
            clauses.append(f"requiring {', '.join(filters['skills'])}")

        summary = f"Found {len(jobs)} active opportunities " + (" ".join(clauses) if clauses else "matching your query.")

        # Persist conversation record
        conv = AIConversation(
            user_id=user_id,
            session_id=session_id,
            user_prompt=query_text,
            extracted_filters=str(filters),
            assistant_response=summary
        )
        self.db.add(conv)
        self.db.commit()

        return jobs, filters, summary
