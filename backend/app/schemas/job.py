from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel

class CompanyBase(BaseModel):
    name: str
    domain: Optional[str] = None
    logo_url: Optional[str] = None
    careers_page_url: Optional[str] = None

class CompanyResponse(CompanyBase):
    id: str

class JobSourceResponse(BaseModel):
    id: str
    name: str
    source_type: str
    base_url: str
    is_enabled: bool

class JobBase(BaseModel):
    title: str
    employment_type: str
    department: Optional[str] = None
    location: str
    country: str = "India"
    city: Optional[str] = None
    remote_type: str
    experience_min: float = 0.0
    experience_max: Optional[float] = None
    salary_min: Optional[float] = None
    salary_max: Optional[float] = None
    salary_currency: str = "INR"
    stipend: Optional[float] = None
    posted_at: datetime
    last_verified_at: datetime
    application_deadline: Optional[datetime] = None
    deadline_source: Optional[str] = None
    application_url: str
    source_url: str
    status: str

class JobResponse(JobBase):
    id: str
    company: CompanyResponse
    source: JobSourceResponse
    match_percentage: Optional[int] = None
    why_matches: List[str] = []
    missing_skills: List[str] = []

class JobDetailResponse(JobResponse):
    description: str
    education_requirements: List[str] = []
    degree_requirements: List[str] = []
    branch_requirements: List[str] = []
    skills: List[str] = []
