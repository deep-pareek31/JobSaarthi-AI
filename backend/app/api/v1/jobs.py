from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.common import ApiResponse, PaginatedResponse
from app.schemas.job import JobDetailResponse, JobResponse
from app.services.job_service import JobService

router = APIRouter(prefix="/jobs", tags=["Jobs"])

@router.get("", response_model=ApiResponse[PaginatedResponse[JobResponse]])
def list_jobs(
    keyword: Optional[str] = Query(None, description="Search across title, description, skills, company"),
    company: Optional[str] = Query(None, description="Filter by company name"),
    employment_type: Optional[str] = Query(None, description="FULL_TIME, INTERNSHIP, CONTRACT, ALL"),
    location: Optional[str] = Query(None, description="City or region name"),
    remote_type: Optional[str] = Query(None, description="REMOTE, HYBRID, ON_SITE, ALL"),
    experience_max: Optional[float] = Query(None, description="Maximum years of experience required"),
    posted_days: Optional[int] = Query(None, description="Posted in last N days (1, 3, 7, 30)"),
    sort_by: str = Query("newest", description="newest, deadline_soon, company, salary"),
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    current_user: Optional[User] = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    items, total = service.get_jobs(
        keyword=keyword,
        company_name=company,
        employment_type=employment_type,
        location=location,
        remote_type=remote_type,
        experience_max=experience_max,
        posted_days=posted_days,
        sort_by=sort_by,
        page=page,
        size=size,
        user=current_user
    )
    pages = (total + size - 1) // size
    return ApiResponse(
        success=True,
        data=PaginatedResponse(items=items, total=total, page=page, size=size, pages=pages)
    )

@router.get("/today", response_model=ApiResponse[List[JobResponse]])
def get_today_openings(
    current_user: Optional[User] = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    results = service.get_today_jobs(user=current_user, limit=10)
    return ApiResponse(success=True, data=results)

@router.get("/recommended", response_model=ApiResponse[List[JobResponse]])
def get_recommended_jobs(
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    results = service.get_recommended_jobs(user=current_user)
    return ApiResponse(success=True, data=results)

@router.get("/saved", response_model=ApiResponse[List[JobResponse]])
def get_saved_jobs(
    category: Optional[str] = Query(None, description="Saved, Apply Later, High Priority, ALL"),
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    results = service.get_saved_jobs(user_id=current_user.id, category=category)
    return ApiResponse(success=True, data=results)

@router.get("/{id}", response_model=ApiResponse[JobDetailResponse])
def get_job_detail(
    id: str,
    current_user: Optional[User] = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    detail = service.get_job_detail(id, user=current_user)
    return ApiResponse(success=True, data=detail)

@router.post("/{id}/save", response_model=ApiResponse[dict])
def save_job(
    id: str,
    category: str = Query("Saved"),
    notes: Optional[str] = Query(None),
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    saved = service.save_job(user_id=current_user.id, job_id=id, category=category, notes=notes)
    return ApiResponse(
        success=True,
        message="Job saved successfully",
        data={"saved_id": saved.id, "job_id": id, "category": saved.category}
    )

@router.delete("/{id}/save", response_model=ApiResponse[dict])
def unsave_job(
    id: str,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = JobService(db)
    success = service.delete_saved_job(user_id=current_user.id, job_id=id)
    return ApiResponse(
        success=success,
        message="Job removed from saved list" if success else "Job was not saved"
    )
