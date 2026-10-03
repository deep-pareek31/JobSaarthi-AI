import json
from typing import List, Optional
from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.api import deps
from app.models.preference import JobPreference
from app.models.user import User
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/preferences", tags=["Preferences"])

class JobPreferenceDto(BaseModel):
    preferred_roles: List[str] = []
    employment_types: List[str] = []
    preferred_locations: List[str] = []
    remote_preferences: List[str] = []
    preferred_companies: List[str] = []
    min_salary: Optional[float] = None

@router.get("", response_model=ApiResponse[JobPreferenceDto])
def get_user_preferences(
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    prefs = current_user.job_preferences
    if not prefs:
        prefs = JobPreference(user_id=current_user.id)
        db.add(prefs)
        db.commit()
        db.refresh(prefs)

    def parse_list(val: str) -> List[str]:
        try:
            res = json.loads(val)
            return res if isinstance(res, list) else []
        except Exception:
            return []

    data = JobPreferenceDto(
        preferred_roles=parse_list(prefs.preferred_roles),
        employment_types=parse_list(prefs.employment_types),
        preferred_locations=parse_list(prefs.preferred_locations),
        remote_preferences=parse_list(prefs.remote_preferences),
        preferred_companies=parse_list(prefs.preferred_companies),
        min_salary=float(prefs.min_salary) if prefs.min_salary is not None else None
    )
    return ApiResponse(success=True, data=data)

@router.patch("", response_model=ApiResponse[JobPreferenceDto])
def update_user_preferences(
    payload: JobPreferenceDto,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    prefs = current_user.job_preferences
    if not prefs:
        prefs = JobPreference(user_id=current_user.id)
        db.add(prefs)

    prefs.preferred_roles = json.dumps(payload.preferred_roles)
    prefs.employment_types = json.dumps(payload.employment_types)
    prefs.preferred_locations = json.dumps(payload.preferred_locations)
    prefs.remote_preferences = json.dumps(payload.remote_preferences)
    prefs.preferred_companies = json.dumps(payload.preferred_companies)
    prefs.min_salary = payload.min_salary

    db.commit()
    db.refresh(prefs)
    return ApiResponse(success=True, message="Preferences updated", data=payload)
