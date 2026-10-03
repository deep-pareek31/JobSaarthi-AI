from typing import Optional
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.models.user import User, Profile
from app.models.subscription import SubscriptionPlanConfig
from app.repositories.user_repository import UserRepository
from app.schemas.user import ProfileResponse, ProfileUpdate, UserDetailResponse

class UserService:
    def __init__(self, db: Session):
        self.db = db
        self.user_repo = UserRepository(db)

    def get_user_details(self, user: User) -> UserDetailResponse:
        profile_res = None
        if user.profile:
            profile_res = ProfileResponse(
                id=user.profile.id,
                user_id=user.id,
                current_location=user.profile.current_location,
                education_level=user.profile.education_level,
                degree=user.profile.degree,
                branch=user.profile.branch,
                institution=user.profile.institution,
                graduation_year=user.profile.graduation_year,
                is_student=user.profile.is_student,
                years_experience=float(user.profile.years_experience or 0.0)
            )

        tier = "FREE"
        plan_name = "Free Tier"
        if user.subscription and user.subscription.plan_config:
            tier = user.subscription.plan_config.plan_tier
            plan_name = user.subscription.plan_config.name

        return UserDetailResponse(
            id=user.id,
            email=user.email,
            full_name=user.full_name,
            role=user.role,
            is_active=user.is_active,
            is_verified=user.is_verified,
            avatar_url=user.avatar_url,
            profile=profile_res,
            subscription_tier=tier,
            plan_name=plan_name
        )

    def update_profile(self, user: User, update_data: ProfileUpdate) -> ProfileResponse:
        profile = user.profile
        if not profile:
            profile = Profile(user_id=user.id)
            self.db.add(profile)

        for field, value in update_data.model_dump(exclude_unset=True).items():
            setattr(profile, field, value)

        self.db.commit()
        self.db.refresh(profile)

        return ProfileResponse(
            id=profile.id,
            user_id=user.id,
            current_location=profile.current_location,
            education_level=profile.education_level,
            degree=profile.degree,
            branch=profile.branch,
            institution=profile.institution,
            graduation_year=profile.graduation_year,
            is_student=profile.is_student,
            years_experience=float(profile.years_experience or 0.0)
        )
