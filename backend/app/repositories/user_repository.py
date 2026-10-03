from typing import Optional
from sqlalchemy.orm import Session
from app.models.user import User, Profile
from app.models.preference import JobPreference, NotificationPreference
from app.models.subscription import Subscription, SubscriptionPlanConfig
from app.core.constants import PlanTier

class UserRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, user_id: str) -> Optional[User]:
        return self.db.query(User).filter(User.id == user_id).first()

    def get_by_email(self, email: str) -> Optional[User]:
        return self.db.query(User).filter(User.email == email.lower().strip()).first()

    def create(self, email: str, hashed_password: Optional[str], full_name: str, phone: Optional[str] = None, role: str = "USER") -> User:
        user = User(
            email=email.lower().strip(),
            hashed_password=hashed_password,
            full_name=full_name.strip(),
            phone=phone,
            role=role,
            is_active=True,
            is_verified=False
        )
        self.db.add(user)
        self.db.flush()

        # Initialize profile
        profile = Profile(user_id=user.id)
        self.db.add(profile)

        # Initialize default preferences
        job_prefs = JobPreference(user_id=user.id)
        self.db.add(job_prefs)

        notif_prefs = NotificationPreference(user_id=user.id)
        self.db.add(notif_prefs)

        # Ensure default FREE plan config exists and assign free subscription
        free_plan = self.db.query(SubscriptionPlanConfig).filter(
            SubscriptionPlanConfig.plan_tier == PlanTier.FREE.value
        ).first()

        if not free_plan:
            free_plan = SubscriptionPlanConfig(
                plan_tier=PlanTier.FREE.value,
                name="Free Starter",
                price=0.00,
                currency="INR",
                billing_cycle="MONTHLY",
                max_active_jobs=3,
                refresh_interval_days=3,
                ai_features_enabled=False,
                expanded_alerts_enabled=False,
                is_active=True
            )
            self.db.add(free_plan)
            self.db.flush()

        subscription = Subscription(
            user_id=user.id,
            plan_config_id=free_plan.id,
            status="ACTIVE"
        )
        self.db.add(subscription)

        self.db.commit()
        self.db.refresh(user)
        return user

    def update_password(self, user: User, hashed_password: str) -> User:
        user.hashed_password = hashed_password
        self.db.commit()
        self.db.refresh(user)
        return user
