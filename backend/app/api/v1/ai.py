from typing import Any, Dict, List
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.common import ApiResponse
from app.schemas.job import JobResponse
from app.services.ai_search_service import AISearchService

router = APIRouter(prefix="/ai", tags=["AI Search Assistant"])

class AISearchRequest(BaseModel):
    query: str
    session_id: str = "default_session"

class AISearchResponse(BaseModel):
    summary: str
    structured_filters: Dict[str, Any]
    results: List[JobResponse]

@router.post("/search", response_model=ApiResponse[AISearchResponse])
def ai_natural_language_search(
    request_data: AISearchRequest,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = AISearchService(db)
    jobs, filters, summary = service.search_with_natural_language(
        user_id=current_user.id,
        query_text=request_data.query,
        session_id=request_data.session_id
    )
    return ApiResponse(
        success=True,
        data=AISearchResponse(
            summary=summary,
            structured_filters=filters,
            results=jobs
        )
    )
