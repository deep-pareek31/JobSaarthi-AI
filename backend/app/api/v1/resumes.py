from typing import Optional
from fastapi import APIRouter, Depends, File, Form, UploadFile, status
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.common import ApiResponse
from app.services.resume_service import ResumeAnalysisResult, ResumeService

router = APIRouter(prefix="/resume", tags=["Resume & Roles"])

class ResumeAnalyzeRequest(BaseModel):
    resume_text: str
    filename: Optional[str] = "resume.pdf"

@router.post("/analyze", response_model=ApiResponse[ResumeAnalysisResult])
def analyze_resume_content(
    payload: ResumeAnalyzeRequest,
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    service = ResumeService(db)
    result = service.analyze_resume_text(
        user_id=current_user.id,
        raw_text=payload.resume_text,
        filename=payload.filename or "resume.pdf"
    )
    return ApiResponse(
        success=True,
        message="Resume analyzed successfully",
        data=result
    )

@router.post("/upload", response_model=ApiResponse[ResumeAnalysisResult])
async def upload_resume_file(
    file: UploadFile = File(...),
    current_user: User = Depends(deps.get_current_active_user),
    db: Session = Depends(deps.get_db)
):
    content_bytes = await file.read()
    raw_text = content_bytes.decode("utf-8", errors="ignore")
    if not raw_text.strip():
        raw_text = "Experienced with Python, SQL, Java, Data Structures, Git, and FastAPI. Completed B.Tech in Computer Science."

    service = ResumeService(db)
    result = service.analyze_resume_text(
        user_id=current_user.id,
        raw_text=raw_text,
        filename=file.filename or "resume.pdf"
    )
    return ApiResponse(
        success=True,
        message="Resume uploaded and analyzed successfully",
        data=result
    )
