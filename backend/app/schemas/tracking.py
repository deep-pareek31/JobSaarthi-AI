from datetime import date, datetime
from typing import Optional
from pydantic import BaseModel
from app.schemas.job import JobResponse

class SavedJobCreate(BaseModel):
    job_id: str
    category: str = "Saved"
    notes: Optional[str] = None

class SavedJobResponse(BaseModel):
    id: str
    job_id: str
    category: str
    notes: Optional[str] = None
    created_at: datetime
    job: JobResponse

class AppliedJobCreate(BaseModel):
    job_id: str
    status: str = "Applied"
    applied_date: date = date.today()
    notes: Optional[str] = None
    resume_used_id: Optional[str] = None

class AppliedJobUpdate(BaseModel):
    status: str
    notes: Optional[str] = None
    remarks: Optional[str] = None

class AppliedJobResponse(BaseModel):
    id: str
    job_id: str
    status: str
    applied_date: date
    notes: Optional[str] = None
    updated_at: datetime
    job: JobResponse
