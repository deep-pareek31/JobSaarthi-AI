from typing import Any, Dict, List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.common import ApiResponse
from app.schemas.tracking import AppliedJobCreate, AppliedJobResponse, AppliedJobUpdate
from app.services.application_service import ApplicationService

router = APIRouter(prefix="/applications", tags=["Applications Tracker"])

@router.post("", response_model=ApiResponse[AppliedJobResponse], status_code=status.HTTP_201_CREATED)
def mark_applied(
    payload: AppliedJobCreate,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = ApplicationService(db)
    result = service.apply_job(user_id=current_user.id, payload=payload)
    return ApiResponse(
        success=True,
        message="Application tracked successfully!",
        data=result
    )

@router.get("", response_model=ApiResponse[List[AppliedJobResponse]])
def get_user_applications(
    status_filter: Optional[str] = Query(None, alias="status"),
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = ApplicationService(db)
    results = service.get_applications(user_id=current_user.id, status_filter=status_filter)
    return ApiResponse(success=True, data=results)

@router.get("/stats", response_model=ApiResponse[Dict[str, int]])
def get_application_statistics(
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = ApplicationService(db)
    stats = service.get_application_stats(user_id=current_user.id)
    return ApiResponse(success=True, data=stats)

@router.patch("/{id}", response_model=ApiResponse[AppliedJobResponse])
def update_application(
    id: str,
    payload: AppliedJobUpdate,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = ApplicationService(db)
    updated = service.update_status(user_id=current_user.id, applied_id=id, payload=payload)
    return ApiResponse(
        success=True,
        message="Application status updated",
        data=updated
    )
