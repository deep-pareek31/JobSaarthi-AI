from sqlalchemy import Column, Date, ForeignKey, String, Text, UniqueConstraint
from sqlalchemy.orm import relationship
from datetime import date
from app.models.base import TimeStampedModel
from app.core.constants import ApplicationStatus, SavedCategory

class SavedJob(TimeStampedModel):
    __tablename__ = "saved_jobs"
    __table_args__ = (
        UniqueConstraint("user_id", "job_id", name="uq_user_saved_job"),
    )

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    job_id = Column(String(36), ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False, index=True)
    category = Column(String(50), default=SavedCategory.SAVED.value, nullable=False)
    notes = Column(Text, nullable=True)

    user = relationship("User", back_populates="saved_jobs")
    job = relationship("Job", back_populates="saved_instances")

class AppliedJob(TimeStampedModel):
    __tablename__ = "applied_jobs"
    __table_args__ = (
        UniqueConstraint("user_id", "job_id", name="uq_user_applied_job"),
    )

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    job_id = Column(String(36), ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False, index=True)
    status = Column(String(50), default=ApplicationStatus.APPLIED.value, nullable=False, index=True)
    applied_date = Column(Date, default=date.today, nullable=False)
    notes = Column(Text, nullable=True)
    resume_used_id = Column(String(36), ForeignKey("resumes.id", ondelete="SET NULL"), nullable=True)

    user = relationship("User", back_populates="applied_jobs")
    job = relationship("Job", back_populates="applied_instances")
    status_history = relationship("ApplicationStatusHistory", back_populates="applied_job", cascade="all, delete-orphan")

class ApplicationStatusHistory(TimeStampedModel):
    __tablename__ = "application_status_history"

    applied_job_id = Column(String(36), ForeignKey("applied_jobs.id", ondelete="CASCADE"), nullable=False, index=True)
    from_status = Column(String(50), nullable=False)
    to_status = Column(String(50), nullable=False)
    remarks = Column(Text, nullable=True)

    applied_job = relationship("AppliedJob", back_populates="status_history")
