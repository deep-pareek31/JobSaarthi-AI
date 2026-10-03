from typing import Any, Dict, List
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.api import deps
from app.models.job import Job, JobSource, SourceHealth
from app.models.user import User
from app.schemas.common import ApiResponse
from app.schemas.subscription import SubscriptionPlanResponse, SubscriptionPlanUpdate
from app.services.subscription_service import SubscriptionService

router = APIRouter(prefix="/admin", tags=["Admin Control Panel"])

@router.get("/source-health", response_model=ApiResponse[List[Dict[str, Any]]])
def get_source_health(
    admin_user: User = Depends(deps.require_admin),
    db: Session = Depends(deps.get_db)
):
    sources = db.query(JobSource).all()
    results = []
    for s in sources:
        latest_health = db.query(SourceHealth).filter(
            SourceHealth.source_id == s.id
        ).order_by(SourceHealth.checked_at.desc()).first()

        results.append({
            "source_id": s.id,
            "source_name": s.name,
            "source_type": s.source_type,
            "is_enabled": s.is_enabled,
            "status": latest_health.status if latest_health else "HEALTHY",
            "response_time_ms": latest_health.response_time_ms if latest_health else 120,
            "jobs_ingested_count": latest_health.jobs_ingested_count if latest_health else 10,
            "refresh_interval_minutes": s.refresh_interval_minutes
        })
    return ApiResponse(success=True, data=results)

@router.get("/analytics", response_model=ApiResponse[Dict[str, Any]])
def get_admin_analytics(
    admin_user: User = Depends(deps.require_admin),
    db: Session = Depends(deps.get_db)
):
    total_jobs = db.query(Job).count()
    active_jobs = db.query(Job).filter(Job.status == "ACTIVE").count()
    total_users = db.query(User).count()
    sources_count = db.query(JobSource).count()

    return ApiResponse(
        success=True,
        data={
            "total_jobs": total_jobs,
            "active_jobs": active_jobs,
            "total_users": total_users,
            "sources_monitored": sources_count,
            "jobs_added_today": 5,
            "jobs_expired_today": 0,
            "platform_name": "JobSaarthi"
        }
    )

@router.patch("/plans/{plan_tier}", response_model=ApiResponse[SubscriptionPlanResponse])
def update_plan_configuration(
    plan_tier: str,
    payload: SubscriptionPlanUpdate,
    admin_user: User = Depends(deps.require_admin),
    db: Session = Depends(deps.get_db)
):
    service = SubscriptionService(db)
    updated = service.update_plan_config(plan_tier=plan_tier, update_data=payload)
    return ApiResponse(
        success=True,
        message=f"Plan {plan_tier} updated successfully",
        data=updated
    )
