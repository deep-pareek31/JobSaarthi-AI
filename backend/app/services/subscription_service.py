from datetime import datetime, timedelta, timezone
from typing import List, Optional
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.core.constants import PlanTier
from app.models.subscription import Subscription, SubscriptionEvent, SubscriptionPlanConfig
from app.models.user import User
from app.schemas.subscription import SubscriptionPlanResponse, SubscriptionPlanUpdate, UserSubscriptionResponse

class SubscriptionService:
    def __init__(self, db: Session):
        self.db = db

    def seed_default_plans(self):
        default_plans = [
            {
                "plan_tier": PlanTier.FREE.value,
                "name": "Free Starter",
                "price": 0.00,
                "currency": "INR",
                "billing_cycle": "MONTHLY",
                "max_active_jobs": 3,
                "refresh_interval_days": 3,
                "ai_features_enabled": False,
                "expanded_alerts_enabled": False,
                "is_active": True
            },
            {
                "plan_tier": PlanTier.PRO_99.value,
                "name": "Pro Career",
                "price": 99.00,
                "currency": "INR",
                "billing_cycle": "MONTHLY",
                "max_active_jobs": 20,
                "refresh_interval_days": 3,
                "ai_features_enabled": False,
                "expanded_alerts_enabled": True,
                "is_active": True
            },
            {
                "plan_tier": PlanTier.ELITE_189.value,
                "name": "Elite Discovery",
                "price": 189.00,
                "currency": "INR",
                "billing_cycle": "MONTHLY",
                "max_active_jobs": -1, # Unlimited
                "refresh_interval_days": 1,
                "ai_features_enabled": True,
                "expanded_alerts_enabled": True,
                "is_active": True
            },
            {
                "plan_tier": PlanTier.PREMIUM_CONFIGURABLE.value,
                "name": "Custom Enterprise",
                "price": 499.00,
                "currency": "INR",
                "billing_cycle": "MONTHLY",
                "max_active_jobs": -1,
                "refresh_interval_days": 1,
                "ai_features_enabled": True,
                "expanded_alerts_enabled": True,
                "is_active": True
            }
        ]

        for p_data in default_plans:
            existing = self.db.query(SubscriptionPlanConfig).filter(
                SubscriptionPlanConfig.plan_tier == p_data["plan_tier"]
            ).first()
            if not existing:
                plan = SubscriptionPlanConfig(**p_data)
                self.db.add(plan)
        self.db.commit()

    def get_plans(self) -> List[SubscriptionPlanResponse]:
        plans = self.db.query(SubscriptionPlanConfig).filter(
            SubscriptionPlanConfig.is_active == True
        ).all()
        return [
            SubscriptionPlanResponse(
                id=p.id,
                plan_tier=p.plan_tier,
                name=p.name,
                price=float(p.price),
                currency=p.currency,
                billing_cycle=p.billing_cycle,
                max_active_jobs=p.max_active_jobs,
                refresh_interval_days=p.refresh_interval_days,
                ai_features_enabled=p.ai_features_enabled,
                expanded_alerts_enabled=p.expanded_alerts_enabled,
                is_active=p.is_active
            )
            for p in plans
        ]

    def subscribe_user(self, user_id: str, plan_tier: str, external_order_id: Optional[str] = None) -> UserSubscriptionResponse:
        plan = self.db.query(SubscriptionPlanConfig).filter(
            SubscriptionPlanConfig.plan_tier == plan_tier,
            SubscriptionPlanConfig.is_active == True
        ).first()

        if not plan:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Subscription plan tier not found")

        subscription = self.db.query(Subscription).filter(Subscription.user_id == user_id).first()
        now = datetime.now(timezone.utc)
        expires_at = now + timedelta(days=30) if plan.price > 0 else None

        if subscription:
            old_plan_id = subscription.plan_config_id
            subscription.plan_config_id = plan.id
            subscription.status = "ACTIVE"
            subscription.starts_at = now
            subscription.expires_at = expires_at
            subscription.external_order_id = external_order_id
        else:
            subscription = Subscription(
                user_id=user_id,
                plan_config_id=plan.id,
                status="ACTIVE",
                starts_at=now,
                expires_at=expires_at,
                external_order_id=external_order_id
            )
            self.db.add(subscription)

        self.db.flush()

        event = SubscriptionEvent(
            subscription_id=subscription.id,
            event_type="UPGRADE" if plan.price > 0 else "SUBSCRIBE",
            event_payload=f"Subscribed to {plan.name} at {plan.price} {plan.currency}"
        )
        self.db.add(event)
        self.db.commit()
        self.db.refresh(subscription)

        return UserSubscriptionResponse(
            id=subscription.id,
            plan=SubscriptionPlanResponse(
                id=plan.id,
                plan_tier=plan.plan_tier,
                name=plan.name,
                price=float(plan.price),
                currency=plan.currency,
                billing_cycle=plan.billing_cycle,
                max_active_jobs=plan.max_active_jobs,
                refresh_interval_days=plan.refresh_interval_days,
                ai_features_enabled=plan.ai_features_enabled,
                expanded_alerts_enabled=plan.expanded_alerts_enabled,
                is_active=plan.is_active
            ),
            status=subscription.status,
            starts_at=subscription.starts_at,
            expires_at=subscription.expires_at
        )

    def update_plan_config(self, plan_tier: str, update_data: SubscriptionPlanUpdate) -> SubscriptionPlanResponse:
        plan = self.db.query(SubscriptionPlanConfig).filter(
            SubscriptionPlanConfig.plan_tier == plan_tier
        ).first()

        if not plan:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Plan not found")

        for key, value in update_data.model_dump(exclude_unset=True).items():
            setattr(plan, key, value)

        self.db.commit()
        self.db.refresh(plan)

        return SubscriptionPlanResponse(
            id=plan.id,
            plan_tier=plan.plan_tier,
            name=plan.name,
            price=float(plan.price),
            currency=plan.currency,
            billing_cycle=plan.billing_cycle,
            max_active_jobs=plan.max_active_jobs,
            refresh_interval_days=plan.refresh_interval_days,
            ai_features_enabled=plan.ai_features_enabled,
            expanded_alerts_enabled=plan.expanded_alerts_enabled,
            is_active=plan.is_active
        )
