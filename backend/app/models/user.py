from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, Numeric, String, Text
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
from app.models.base import TimeStampedModel
from app.core.constants import UserRole

class User(TimeStampedModel):
    __tablename__ = "users"

    email = Column(String(255), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=True) # Nullable for Google OAuth
    full_name = Column(String(150), nullable=False)
    phone = Column(String(30), nullable=True)
    role = Column(String(20), default=UserRole.USER.value, nullable=False)
    is_active = Column(Boolean, default=True, nullable=False)
    is_verified = Column(Boolean, default=False, nullable=False)
    avatar_url = Column(String(500), nullable=True)

    # Relationships
    profile = relationship("Profile", back_populates="user", uselist=False, cascade="all, delete-orphan")
    resumes = relationship("Resume", back_populates="user", cascade="all, delete-orphan")
    job_preferences = relationship("JobPreference", back_populates="user", uselist=False, cascade="all, delete-orphan")
    saved_jobs = relationship("SavedJob", back_populates="user", cascade="all, delete-orphan")
    applied_jobs = relationship("AppliedJob", back_populates="user", cascade="all, delete-orphan")
    subscription = relationship("Subscription", back_populates="user", uselist=False, cascade="all, delete-orphan")
    notifications = relationship("Notification", back_populates="user", cascade="all, delete-orphan")
    notification_preferences = relationship("NotificationPreference", back_populates="user", uselist=False, cascade="all, delete-orphan")

class Profile(TimeStampedModel):
    __tablename__ = "profiles"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    current_location = Column(String(150), nullable=True)
    education_level = Column(String(100), nullable=True)
    degree = Column(String(100), nullable=True)
    branch = Column(String(100), nullable=True)
    institution = Column(String(255), nullable=True)
    graduation_year = Column(Integer, nullable=True)
    is_student = Column(Boolean, default=False, nullable=False)
    years_experience = Column(Numeric(4, 1), default=0.0, nullable=False)

    user = relationship("User", back_populates="profile")

class AdminUser(TimeStampedModel):
    __tablename__ = "admin_users"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    department = Column(String(100), nullable=True)
    permissions = Column(Text, default="[]", nullable=False) # JSON array of permissions
    last_login_at = Column(DateTime(timezone=True), nullable=True)

class AuditLog(TimeStampedModel):
    __tablename__ = "audit_logs"

    actor_id = Column(String(36), ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    action = Column(String(100), nullable=False)
    resource_type = Column(String(100), nullable=False)
    resource_id = Column(String(100), nullable=True)
    details = Column(Text, nullable=True) # JSON or descriptive string
    ip_address = Column(String(50), nullable=True)
