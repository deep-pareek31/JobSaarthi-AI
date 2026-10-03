from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.common import ApiResponse
from app.schemas.user import ProfileResponse, ProfileUpdate, UserDetailResponse
from app.services.user_service import UserService

router = APIRouter(prefix="/users", tags=["Users"])

@router.get("/me", response_model=ApiResponse[UserDetailResponse])
def get_user_profile_detail(
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = UserService(db)
    details = service.get_user_details(current_user)
    return ApiResponse(success=True, data=details)

@router.patch("/me/profile", response_model=ApiResponse[ProfileResponse])
def update_user_profile(
    profile_data: ProfileUpdate,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = UserService(db)
    updated = service.update_profile(current_user, profile_data)
    return ApiResponse(
        success=True,
        message="Profile updated successfully",
        data=updated
    )
