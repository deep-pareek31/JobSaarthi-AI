from abc import ABC, abstractmethod
from datetime import datetime
from typing import Any, Dict, List, Optional
from pydantic import BaseModel

class RawJobData(BaseModel):
    source_name: str
    external_job_id: str
    raw_payload: Dict[str, Any]

class NormalizedJobPayload(BaseModel):
    source_name: str
    external_job_id: str
    company_name: str
    company_domain: Optional[str] = None
    title: str
    normalized_title: str
    description: str
    employment_type: str # FULL_TIME, INTERNSHIP, etc.
    location: str
    city: Optional[str] = None
    state: Optional[str] = None
    country: str = "India"
    remote_type: str # REMOTE, HYBRID, ON_SITE
    experience_min: float = 0.0
    experience_max: Optional[float] = None
    skills: List[str] = []
    salary_min: Optional[float] = None
    salary_max: Optional[float] = None
    salary_currency: str = "INR"
    stipend: Optional[float] = None
    posted_at: datetime
    application_deadline: Optional[datetime] = None
    deadline_source: Optional[str] = None
    application_url: str
    source_url: str

class JobSourceConnector(ABC):
    """
    Abstract interface for all JobSaarthi ingestion connectors.
    Every connector respects rate limits, handles errors gracefully,
    and returns standardized normalized payloads.
    """

    @abstractmethod
    def get_source_name(self) -> str:
        """Returns the identifier name of the source (e.g. 'greenhouse_stripe')."""
        pass

    @abstractmethod
    def fetch_jobs(self, since: Optional[datetime] = None) -> List[RawJobData]:
        """Fetches active job listings from the legitimate public/official endpoint."""
        pass

    @abstractmethod
    def fetch_job_details(self, external_job_id: str) -> Optional[RawJobData]:
        """Fetches complete description and application details for a specific listing."""
        pass

    @abstractmethod
    def validate_job(self, external_job_id: str) -> bool:
        """Checks if a previously indexed job is still active on the source."""
        pass

    @abstractmethod
    def detect_closed_job(self, external_job_id: str) -> bool:
        """Checks if source explicitly indicates the posting has been closed."""
        pass

    @abstractmethod
    def normalize_job(self, raw_job: RawJobData) -> NormalizedJobPayload:
        """Transforms vendor-specific payload into JobSaarthi normalized format."""
        pass
