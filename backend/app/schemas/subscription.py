from datetime import datetime
from typing import Optional
from pydantic import BaseModel

class SubscriptionPlanResponse(BaseModel):
    id: str
    plan_tier: str
    name: str
    price: float
    currency: str
    billing_cycle: str
    max_active_jobs: int
    refresh_interval_days: int
    ai_features_enabled: bool
    expanded_alerts_enabled: bool
    is_active: bool

class SubscriptionPlanUpdate(BaseModel):
    name: Optional[str] = None
    price: Optional[float] = None
    max_active_jobs: Optional[int] = None
    refresh_interval_days: Optional[int] = None
    ai_features_enabled: Optional[bool] = None
    expanded_alerts_enabled: Optional[bool] = None
    is_active: Optional[bool] = None

class UserSubscriptionResponse(BaseModel):
    id: str
    plan: SubscriptionPlanResponse
    status: str
    starts_at: datetime
    expires_at: Optional[datetime] = None
