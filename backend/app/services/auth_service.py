from datetime import datetime, timezone
import json
from typing import Optional, Tuple
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.core import security
from app.core.config import settings
from app.models.user import User
from app.repositories.user_repository import UserRepository
from app.repositories.audit_repository import AuditRepository
from app.schemas.auth import (
    AuthSuccessResponse,
    LoginRequest,
    RegisterRequest,
    TokenPair,
    UserAuthResponse,
)

class AuthService:
    def __init__(self, db: Session):
        self.db = db
        self.user_repo = UserRepository(db)
        self.audit_repo = AuditRepository(db)

    def register_user(self, request: RegisterRequest, ip_address: Optional[str] = None) -> AuthSuccessResponse:
        existing = self.user_repo.get_by_email(request.email)
        if existing:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="An account with this email address already exists."
            )
        
        hashed_password = security.get_password_hash(request.password)
        user = self.user_repo.create(
            email=request.email,
            hashed_password=hashed_password,
            full_name=request.full_name,
            phone=request.phone,
            role="USER"
        )

        self.audit_repo.log(
            action="USER_REGISTERED",
            resource_type="USER",
            resource_id=user.id,
            actor_id=user.id,
            details=f"User registered with email {user.email}",
            ip_address=ip_address
        )

        return self._generate_auth_response(user)

    def authenticate_user(self, request: LoginRequest, ip_address: Optional[str] = None) -> AuthSuccessResponse:
        user = self.user_repo.get_by_email(request.email)
        if not user or not user.hashed_password:
            self.audit_repo.log(
                action="LOGIN_FAILED",
                resource_type="USER",
                details=f"Failed login attempt for email {request.email}",
                ip_address=ip_address
            )
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect email or password.",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not security.verify_password(request.password, user.hashed_password):
            self.audit_repo.log(
                action="LOGIN_FAILED_PASSWORD",
                resource_type="USER",
                resource_id=user.id,
                actor_id=user.id,
                details=f"Incorrect password for email {request.email}",
                ip_address=ip_address
            )
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect email or password.",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Your account has been deactivated. Please contact support."
            )

        self.audit_repo.log(
            action="LOGIN_SUCCESS",
            resource_type="USER",
            resource_id=user.id,
            actor_id=user.id,
            details="User logged in successfully",
            ip_address=ip_address
        )

        return self._generate_auth_response(user)

    def refresh_tokens(self, refresh_token: str, ip_address: Optional[str] = None) -> TokenPair:
        try:
            payload = security.decode_token(refresh_token)
            if payload.get("type") != "refresh":
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid token type for refresh."
                )
            user_id = payload.get("sub")
            if not user_id:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid token payload."
                )
        except Exception:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Expired or invalid refresh token."
            )

        user = self.user_repo.get_by_id(user_id)
        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User account no longer active."
            )

        new_access = security.create_access_token(subject=user.id, role=user.role)
        new_refresh = security.create_refresh_token(subject=user.id)

        self.audit_repo.log(
            action="TOKEN_REFRESH",
            resource_type="USER",
            resource_id=user.id,
            actor_id=user.id,
            ip_address=ip_address
        )

        return TokenPair(
            access_token=new_access,
            refresh_token=new_refresh,
            token_type="bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
        )

    def authenticate_google(self, id_token_str: str, ip_address: Optional[str] = None) -> AuthSuccessResponse:
        from google.oauth2 import id_token
        from google.auth.transport import requests

        email: str
        full_name: str
        avatar_url: Optional[str] = None

        try:
            # Verify against configured client ID or accept if Google-issued in dev
            client_ids = [cid for cid in [settings.GOOGLE_CLIENT_ID, settings.GOOGLE_ANDROID_CLIENT_ID] if cid]
            id_info = id_token.verify_oauth2_token(
                id_token_str,
                requests.Request(),
                audience=client_ids[0] if client_ids else None
            )
            email = id_info.get("email")
            full_name = id_info.get("name", "Google User")
            avatar_url = id_info.get("picture")
            if not email:
                raise ValueError("No email in token")
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Google ID token verification failed: {str(e)}"
            )

        user = self.user_repo.get_by_email(email)
        if not user:
            user = self.user_repo.create(
                email=email,
                hashed_password=None,
                full_name=full_name,
                role="USER"
            )
            if avatar_url:
                user.avatar_url = avatar_url
                self.db.commit()
            
            self.audit_repo.log(
                action="USER_REGISTERED_GOOGLE",
                resource_type="USER",
                resource_id=user.id,
                actor_id=user.id,
                details=f"User signed up via Google: {email}",
                ip_address=ip_address
            )
        else:
            self.audit_repo.log(
                action="LOGIN_SUCCESS_GOOGLE",
                resource_type="USER",
                resource_id=user.id,
                actor_id=user.id,
                details=f"User signed in via Google: {email}",
                ip_address=ip_address
            )

        return self._generate_auth_response(user)

    def _generate_auth_response(self, user: User) -> AuthSuccessResponse:
        access_token = security.create_access_token(subject=user.id, role=user.role)
        refresh_token = security.create_refresh_token(subject=user.id)

        user_auth = UserAuthResponse(
            id=user.id,
            email=user.email,
            full_name=user.full_name,
            role=user.role,
            is_active=user.is_active,
            is_verified=user.is_verified,
            avatar_url=user.avatar_url
        )

        tokens = TokenPair(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
        )

        return AuthSuccessResponse(user=user_auth, tokens=tokens)
