from typing import List, Optional
from pydantic import BaseModel, EmailStr
from app.schemas.auth import UserAuthResponse

class ProfileBase(BaseModel):
    current_location: Optional[str] = None
    education_level: Optional[str] = None
    degree: Optional[str] = None
    branch: Optional[str] = None
    institution: Optional[str] = None
    graduation_year: Optional[int] = None
    is_student: bool = False
    years_experience: float = 0.0

class ProfileUpdate(ProfileBase):
    pass

class ProfileResponse(ProfileBase):
    id: str
    user_id: str

class UserDetailResponse(UserAuthResponse):
    profile: Optional[ProfileResponse] = None
    subscription_tier: str = "FREE"
    plan_name: str = "Free Tier"
