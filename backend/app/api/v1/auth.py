from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from app.api import deps
from app.models.user import User
from app.schemas.auth import (
    AuthSuccessResponse,
    GoogleAuthRequest,
    LoginRequest,
    RefreshTokenRequest,
    RegisterRequest,
    TokenPair,
    UserAuthResponse,
)
from app.schemas.common import ApiResponse
from app.services.auth_service import AuthService

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register", response_model=ApiResponse[AuthSuccessResponse], status_code=status.HTTP_201_CREATED)
def register(
    request_data: RegisterRequest,
    req: Request,
    db: Session = Depends(deps.get_db)
):
    ip_address = req.client.host if req.client else None
    service = AuthService(db)
    result = service.register_user(request_data, ip_address=ip_address)
    return ApiResponse(
        success=True,
        message="Registration successful. Welcome to JobSaarthi!",
        data=result
    )

@router.post("/login", response_model=ApiResponse[AuthSuccessResponse])
def login(
    request_data: LoginRequest,
    req: Request,
    db: Session = Depends(deps.get_db)
):
    ip_address = req.client.host if req.client else None
    service = AuthService(db)
    result = service.authenticate_user(request_data, ip_address=ip_address)
    return ApiResponse(
        success=True,
        message="Login successful",
        data=result
    )

@router.post("/refresh", response_model=ApiResponse[TokenPair])
def refresh_token(
    request_data: RefreshTokenRequest,
    req: Request,
    db: Session = Depends(deps.get_db)
):
    ip_address = req.client.host if req.client else None
    service = AuthService(db)
    result = service.refresh_tokens(request_data.refresh_token, ip_address=ip_address)
    return ApiResponse(
        success=True,
        message="Token refreshed successfully",
        data=result
    )

@router.post("/google", response_model=ApiResponse[AuthSuccessResponse])
def google_auth(
    request_data: GoogleAuthRequest,
    req: Request,
    db: Session = Depends(deps.get_db)
):
    ip_address = req.client.host if req.client else None
    service = AuthService(db)
    result = service.authenticate_google(request_data.id_token, ip_address=ip_address)
    return ApiResponse(
        success=True,
        message="Google sign-in successful",
        data=result
    )

@router.get("/me", response_model=ApiResponse[UserAuthResponse])
def get_current_user_info(
    current_user: User = Depends(deps.get_current_active_user)
):
    return ApiResponse(
        success=True,
        data=UserAuthResponse(
            id=current_user.id,
            email=current_user.email,
            full_name=current_user.full_name,
            role=current_user.role,
            is_active=current_user.is_active,
            is_verified=current_user.is_verified,
            avatar_url=current_user.avatar_url
        )
    )
