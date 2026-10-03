from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, Numeric, String, Text
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
from app.models.base import TimeStampedModel
from app.core.constants import PlanTier

class SubscriptionPlanConfig(TimeStampedModel):
    __tablename__ = "subscription_plan_configs"

    plan_tier = Column(String(50), unique=True, nullable=False, index=True) # FREE, PRO_99, ELITE_189, PREMIUM_CONFIGURABLE
    name = Column(String(100), nullable=False)
    price = Column(Numeric(10, 2), nullable=False)
    currency = Column(String(10), default="INR", nullable=False)
    billing_cycle = Column(String(50), default="MONTHLY", nullable=False)
    max_active_jobs = Column(Integer, nullable=False) # 3 for Free, 20 for Pro, -1 for Unlimited
    refresh_interval_days = Column(Integer, default=3, nullable=False)
    ai_features_enabled = Column(Boolean, default=False, nullable=False)
    expanded_alerts_enabled = Column(Boolean, default=False, nullable=False)
    is_active = Column(Boolean, default=True, nullable=False)

    subscriptions = relationship("Subscription", back_populates="plan_config")

class Subscription(TimeStampedModel):
    __tablename__ = "subscriptions"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    plan_config_id = Column(String(36), ForeignKey("subscription_plan_configs.id"), nullable=False)
    status = Column(String(50), default="ACTIVE", nullable=False) # ACTIVE, PAST_DUE, CANCELLED, EXPIRED
    starts_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)
    expires_at = Column(DateTime(timezone=True), nullable=True) # None for free lifetime
    external_order_id = Column(String(255), nullable=True)

    user = relationship("User", back_populates="subscription")
    plan_config = relationship("SubscriptionPlanConfig", back_populates="subscriptions")
    events = relationship("SubscriptionEvent", back_populates="subscription", cascade="all, delete-orphan")

class SubscriptionEvent(TimeStampedModel):
    __tablename__ = "subscription_events"

    subscription_id = Column(String(36), ForeignKey("subscriptions.id", ondelete="CASCADE"), nullable=False, index=True)
    event_type = Column(String(50), nullable=False) # PURCHASE, RENEWAL, CANCEL, UPGRADE, DOWNGRADE, EXPIRE
    event_payload = Column(Text, nullable=True) # JSON raw payload
