from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, Numeric, String, Text, UniqueConstraint
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
from app.models.base import TimeStampedModel
from app.core.constants import JobStatus, RemoteType, SourceType

class Company(TimeStampedModel):
    __tablename__ = "companies"

    name = Column(String(255), unique=True, nullable=False, index=True)
    normalized_name = Column(String(255), nullable=False, index=True)
    domain = Column(String(255), nullable=True)
    logo_url = Column(String(500), nullable=True)
    careers_page_url = Column(String(500), nullable=True)

    jobs = relationship("Job", back_populates="company", cascade="all, delete-orphan")

class JobSource(TimeStampedModel):
    __tablename__ = "job_sources"

    name = Column(String(150), unique=True, nullable=False)
    source_type = Column(String(50), default=SourceType.OFFICIAL_COMPANY.value, nullable=False)
    base_url = Column(String(500), nullable=False)
    api_endpoint = Column(String(500), nullable=True)
    auth_config = Column(Text, nullable=True) # JSON encrypted/secure config
    parser_config = Column(Text, nullable=True) # JSON selector or API mapping rules
    refresh_interval_minutes = Column(Integer, default=60, nullable=False)
    rate_limit_per_minute = Column(Integer, default=30, nullable=False)
    is_enabled = Column(Boolean, default=True, nullable=False)
    priority = Column(Integer, default=1, nullable=False)
    last_successful_run = Column(DateTime(timezone=True), nullable=True)
    last_failed_run = Column(DateTime(timezone=True), nullable=True)

    jobs = relationship("Job", back_populates="source")
    health_records = relationship("SourceHealth", back_populates="source", cascade="all, delete-orphan")

class Job(TimeStampedModel):
    __tablename__ = "jobs"
    __table_args__ = (
        UniqueConstraint("source_id", "external_job_id", name="uq_source_external_job"),
    )

    source_id = Column(String(36), ForeignKey("job_sources.id", ondelete="RESTRICT"), nullable=False, index=True)
    company_id = Column(String(36), ForeignKey("companies.id", ondelete="CASCADE"), nullable=False, index=True)
    external_job_id = Column(String(255), nullable=False)
    title = Column(String(300), nullable=False, index=True)
    normalized_title = Column(String(300), nullable=False, index=True)
    description = Column(Text, nullable=False)
    normalized_description = Column(Text, nullable=True)
    employment_type = Column(String(50), nullable=False, index=True) # FULL_TIME, INTERNSHIP, etc.
    job_category = Column(String(100), nullable=True, index=True)
    department = Column(String(100), nullable=True)
    location = Column(String(200), nullable=False, index=True)
    country = Column(String(100), default="India", nullable=False)
    state = Column(String(100), nullable=True)
    city = Column(String(100), nullable=True, index=True)
    remote_type = Column(String(50), default=RemoteType.ON_SITE.value, nullable=False)
    experience_min = Column(Numeric(4, 1), default=0.0, nullable=False)
    experience_max = Column(Numeric(4, 1), nullable=True)
    education_requirements = Column(Text, default="[]", nullable=False) # JSON array
    degree_requirements = Column(Text, default="[]", nullable=False) # JSON array
    branch_requirements = Column(Text, default="[]", nullable=False) # JSON array
    graduation_year_min = Column(Integer, nullable=True)
    graduation_year_max = Column(Integer, nullable=True)
    skills = Column(Text, default="[]", nullable=False) # JSON array of skills
    salary_min = Column(Numeric(12, 2), nullable=True)
    salary_max = Column(Numeric(12, 2), nullable=True)
    salary_currency = Column(String(10), default="INR", nullable=False)
    stipend = Column(Numeric(12, 2), nullable=True)
    posted_at = Column(DateTime(timezone=True), nullable=False, index=True)
    last_verified_at = Column(DateTime(timezone=True), nullable=False, index=True)
    application_deadline = Column(DateTime(timezone=True), nullable=True)
    deadline_source = Column(String(100), nullable=True)
    application_url = Column(String(1000), nullable=False)
    source_url = Column(String(1000), nullable=False)
    status = Column(String(50), default=JobStatus.ACTIVE.value, nullable=False, index=True)
    first_seen_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)
    last_seen_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)

    source = relationship("JobSource", back_populates="jobs")
    company = relationship("Company", back_populates="jobs")
    saved_instances = relationship("SavedJob", back_populates="job", cascade="all, delete-orphan")
    applied_instances = relationship("AppliedJob", back_populates="job", cascade="all, delete-orphan")
    skills_rel = relationship("JobSkill", back_populates="job", cascade="all, delete-orphan")
    locations_rel = relationship("JobLocation", back_populates="job", cascade="all, delete-orphan")
    eligibility_rel = relationship("JobEligibility", back_populates="job", uselist=False, cascade="all, delete-orphan")
    history_rel = relationship("JobHistory", back_populates="job", cascade="all, delete-orphan")

class JobSkill(TimeStampedModel):
    __tablename__ = "job_skills"

    job_id = Column(String(36), ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False, index=True)
    skill_name = Column(String(100), nullable=False, index=True)
    is_required = Column(Boolean, default=True, nullable=False)

    job = relationship("Job", back_populates="skills_rel")

class JobLocation(TimeStampedModel):
    __tablename__ = "job_locations"

    job_id = Column(String(36), ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False, index=True)
    city = Column(String(100), nullable=True)
    state = Column(String(100), nullable=True)
    country = Column(String(100), default="India", nullable=False)
    is_primary = Column(Boolean, default=True, nullable=False)

    job = relationship("Job", back_populates="locations_rel")

class JobEligibility(TimeStampedModel):
    __tablename__ = "job_eligibility"

    job_id = Column(String(36), ForeignKey("jobs.id", ondelete="CASCADE"), unique=True, nullable=False)
    criteria_summary = Column(Text, nullable=True)
    cutoff_cgpa = Column(Numeric(3, 2), nullable=True)
    allowed_branches = Column(Text, default="[]", nullable=False) # JSON array
    allowed_batch_years = Column(Text, default="[]", nullable=False) # JSON array

    job = relationship("Job", back_populates="eligibility_rel")

class JobHistory(TimeStampedModel):
    __tablename__ = "job_history"

    job_id = Column(String(36), ForeignKey("jobs.id", ondelete="CASCADE"), nullable=False, index=True)
    event_type = Column(String(50), nullable=False) # 'DISCOVERED', 'VERIFIED', 'MODIFIED', 'CLOSED', 'EXPIRED'
    notes = Column(Text, nullable=True)

    job = relationship("Job", back_populates="history_rel")

class SourceHealth(TimeStampedModel):
    __tablename__ = "source_health"

    source_id = Column(String(36), ForeignKey("job_sources.id", ondelete="CASCADE"), nullable=False, index=True)
    status = Column(String(50), nullable=False) # 'HEALTHY', 'DEGRADED', 'FAILED'
    response_time_ms = Column(Integer, nullable=True)
    jobs_ingested_count = Column(Integer, default=0)
    error_message = Column(Text, nullable=True)
    checked_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)

    source = relationship("JobSource", back_populates="health_records")
