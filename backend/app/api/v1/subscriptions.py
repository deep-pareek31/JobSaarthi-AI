from typing import List, Optional
from fastapi import APIRouter, Depends, status
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.common import ApiResponse
from app.schemas.subscription import SubscriptionPlanResponse, UserSubscriptionResponse
from app.services.subscription_service import SubscriptionService

router = APIRouter(tags=["Subscriptions"])

class SubscribeRequest(BaseModel):
    plan_tier: str
    external_order_id: Optional[str] = None

@router.get("/plans", response_model=ApiResponse[List[SubscriptionPlanResponse]])
def list_available_plans(
    db: Session = Depends(deps.get_db)
):
    service = SubscriptionService(db)
    service.seed_default_plans()
    plans = service.get_plans()
    return ApiResponse(success=True, data=plans)

@router.post("/subscriptions/subscribe", response_model=ApiResponse[UserSubscriptionResponse])
def subscribe_plan(
    payload: SubscribeRequest,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = SubscriptionService(db)
    sub = service.subscribe_user(
        user_id=current_user.id,
        plan_tier=payload.plan_tier,
        external_order_id=payload.external_order_id
    )
    return ApiResponse(
        success=True,
        message=f"Subscribed to {sub.plan.name}!",
        data=sub
    )
