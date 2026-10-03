from datetime import datetime, timezone
from typing import Any, Dict, List, Optional
import httpx
from workers.connectors.base import JobSourceConnector, NormalizedJobPayload, RawJobData

class LeverJobConnector(JobSourceConnector):
    """
    Public Postings API connector for companies hosted on Lever.co.
    Endpoint: https://api.lever.co/v0/postings/{company_site}?mode=json
    """

    def __init__(self, company_site: str, company_name: str, company_domain: Optional[str] = None):
        self.company_site = company_site
        self.company_name = company_name
        self.company_domain = company_domain
        self.base_url = f"https://api.lever.co/v0/postings/{company_site}"

    def get_source_name(self) -> str:
        return f"lever_{self.company_site}"

    def fetch_jobs(self, since: Optional[datetime] = None) -> List[RawJobData]:
        url = f"{self.base_url}?mode=json"
        with httpx.Client(timeout=15.0) as client:
            resp = client.get(url)
            if resp.status_code != 200:
                return []
            postings = resp.json()
            return [
                RawJobData(
                    source_name=self.get_source_name(),
                    external_job_id=str(item["id"]),
                    raw_payload=item
                )
                for item in postings
            ]

    def fetch_job_details(self, external_job_id: str) -> Optional[RawJobData]:
        url = f"{self.base_url}/{external_job_id}"
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
        url = f"{self.base_url}/{external_job_id}"
        with httpx.Client(timeout=10.0) as client:
            resp = client.get(url)
            return resp.status_code == 200

    def detect_closed_job(self, external_job_id: str) -> bool:
        return not self.validate_job(external_job_id)

    def normalize_job(self, raw_job: RawJobData) -> NormalizedJobPayload:
        p = raw_job.raw_payload
        title = p.get("text", "Untitled Role")
        desc = p.get("descriptionPlain", "")
        cats = p.get("categories", {})
        loc = cats.get("location", "Remote")
        commitment = cats.get("commitment", "Full time")

        created_ts = p.get("createdAt")
        posted_at = datetime.fromtimestamp(created_ts / 1000.0, tz=timezone.utc) if created_ts else datetime.now(timezone.utc)

        emp_type = "FULL_TIME"
        if "intern" in commitment.lower() or "intern" in title.lower():
            emp_type = "INTERNSHIP"

        remote_type = "ON_SITE"
        if cats.get("workplaceType") == "remote" or "remote" in loc.lower():
            remote_type = "REMOTE"
        elif cats.get("workplaceType") == "hybrid" or "hybrid" in loc.lower():
            remote_type = "HYBRID"

        apply_url = p.get("applyUrl", p.get("hostedUrl", ""))

        return NormalizedJobPayload(
            source_name=self.get_source_name(),
            external_job_id=raw_job.external_job_id,
            company_name=self.company_name,
            company_domain=self.company_domain,
            title=title,
            normalized_title=title.strip(),
            description=desc,
            employment_type=emp_type,
            location=loc,
            remote_type=remote_type,
            posted_at=posted_at,
            application_deadline=None,
            deadline_source="Application deadline not specified by employer.",
            application_url=apply_url,
            source_url=p.get("hostedUrl", apply_url)
        )
