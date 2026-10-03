from sqlalchemy import Boolean, Column, ForeignKey, Numeric, String, Text
from sqlalchemy.orm import relationship
from app.models.base import TimeStampedModel

class JobPreference(TimeStampedModel):
    __tablename__ = "job_preferences"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    preferred_roles = Column(Text, default="[]", nullable=False) # JSON array of roles
    employment_types = Column(Text, default="[]", nullable=False) # JSON array
    preferred_locations = Column(Text, default="[]", nullable=False) # JSON array
    remote_preferences = Column(Text, default="[]", nullable=False) # JSON array
    preferred_companies = Column(Text, default="[]", nullable=False) # JSON array
    excluded_companies = Column(Text, default="[]", nullable=False) # JSON array
    min_salary = Column(Numeric(12, 2), nullable=True)
    salary_currency = Column(String(10), default="INR", nullable=False)

    user = relationship("User", back_populates="job_preferences")

class SearchHistory(TimeStampedModel):
    __tablename__ = "search_history"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    raw_query = Column(String(500), nullable=False)
    parsed_filters = Column(Text, nullable=True) # JSON of structured filters derived
    results_count = Column(Numeric, default=0)

class NotificationPreference(TimeStampedModel):
    __tablename__ = "notification_preferences"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    new_matching_jobs = Column(Boolean, default=True, nullable=False)
    preferred_companies = Column(Boolean, default=True, nullable=False)
    deadline_reminders = Column(Boolean, default=True, nullable=False)
    saved_job_expiration = Column(Boolean, default=True, nullable=False)
    application_reminders = Column(Boolean, default=True, nullable=False)
    subscription_reminders = Column(Boolean, default=True, nullable=False)

    user = relationship("User", back_populates="notification_preferences")
