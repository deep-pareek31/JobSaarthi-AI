from datetime import datetime, timezone
from typing import Any, Dict, List, Optional
import httpx
from dateutil import parser
from workers.connectors.base import JobSourceConnector, NormalizedJobPayload, RawJobData

class GreenhouseJobConnector(JobSourceConnector):
    """
    Public Board API connector for companies hosted on Greenhouse ATS.
    Endpoint: https://boards-api.greenhouse.io/v1/boards/{board_token}/jobs
    """

    def __init__(self, board_token: str, company_name: str, company_domain: Optional[str] = None):
        self.board_token = board_token
        self.company_name = company_name
        self.company_domain = company_domain
        self.base_url = f"https://boards-api.greenhouse.io/v1/boards/{board_token}"

    def get_source_name(self) -> str:
        return f"greenhouse_{self.board_token}"

    def fetch_jobs(self, since: Optional[datetime] = None) -> List[RawJobData]:
        url = f"{self.base_url}/jobs?content=true"
        with httpx.Client(timeout=15.0) as client:
            resp = client.get(url)
            if resp.status_code != 200:
                return []
            data = resp.json()
            jobs = data.get("jobs", [])
            return [
                RawJobData(
                    source_name=self.get_source_name(),
                    external_job_id=str(job["id"]),
                    raw_payload=job
                )
                for job in jobs
            ]

    def fetch_job_details(self, external_job_id: str) -> Optional[RawJobData]:
        url = f"{self.base_url}/jobs/{external_job_id}"
        with httpx.Client(timeout=15.0) as client:
            resp = client.get(url)
            if resp.status_code == 200:
                return RawJobData(
                    source_name=self.get_source_name(),
                    external_job_id=external_job_id,
                    raw_payload=resp.json()
                )
        return None

    def validate_job(self, external_job_id: str) -> bool:
        url = f"{self.base_url}/jobs/{external_job_id}"
        with httpx.Client(timeout=10.0) as client:
            resp = client.get(url)
            return resp.status_code == 200

    def detect_closed_job(self, external_job_id: str) -> bool:
        return not self.validate_job(external_job_id)

    def normalize_job(self, raw_job: RawJobData) -> NormalizedJobPayload:
        p = raw_job.raw_payload
        title = p.get("title", "Untitled Role")
        content = p.get("content", "")
        location_obj = p.get("location", {})
        location_name = location_obj.get("name", "Remote") if isinstance(location_obj, dict) else str(location_obj)

        updated_at_str = p.get("updated_at")
        posted_at = parser.parse(updated_at_str) if updated_at_str else datetime.now(timezone.utc)

        # Detect employment type
        emp_type = "FULL_TIME"
        title_lower = title.lower()
        if "intern" in title_lower or "trainee" in title_lower:
            emp_type = "INTERNSHIP"
        elif "contract" in title_lower:
            emp_type = "CONTRACT"

        remote_type = "ON_SITE"
        if "remote" in location_name.lower() or "remote" in title_lower:
            remote_type = "REMOTE"
        elif "hybrid" in location_name.lower() or "hybrid" in title_lower:
            remote_type = "HYBRID"

        return NormalizedJobPayload(
            source_name=self.get_source_name(),
            external_job_id=raw_job.external_job_id,
            company_name=self.company_name,
            company_domain=self.company_domain,
            title=title,
            normalized_title=title.strip(),
            description=content,
            employment_type=emp_type,
            location=location_name,
            remote_type=remote_type,
            posted_at=posted_at,
            application_deadline=None, # Greenhouse public API does not declare deadlines unless specified in body
            deadline_source="Application deadline not specified by employer.",
            application_url=p.get("absolute_url", f"https://boards.greenhouse.io/{self.board_token}/jobs/{raw_job.external_job_id}"),
            source_url=p.get("absolute_url", f"https://boards.greenhouse.io/{self.board_token}/jobs/{raw_job.external_job_id}")
        )
