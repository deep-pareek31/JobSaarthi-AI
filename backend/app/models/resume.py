from sqlalchemy import Column, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship
from app.models.base import TimeStampedModel

class Resume(TimeStampedModel):
    __tablename__ = "resumes"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    storage_key = Column(String(500), nullable=False)
    file_name = Column(String(255), nullable=False)
    mime_type = Column(String(100), nullable=False)
    file_size_bytes = Column(Integer, nullable=False)
    raw_text = Column(Text, nullable=True)
    parsed_json = Column(Text, nullable=True) # JSON structured representation of resume data

    user = relationship("User", back_populates="resumes")
    skills = relationship("ResumeSkill", back_populates="resume", cascade="all, delete-orphan")

class ResumeSkill(TimeStampedModel):
    __tablename__ = "resume_skills"

    resume_id = Column(String(36), ForeignKey("resumes.id", ondelete="CASCADE"), nullable=False, index=True)
    skill_name = Column(String(100), nullable=False, index=True)
    category = Column(String(50), nullable=True) # LANGUAGE, FRAMEWORK, DATABASE, TOOL
    proficiency_evidence = Column(Text, nullable=True)

    resume = relationship("Resume", back_populates="skills")
