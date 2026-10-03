import enum

class UserRole(str, enum.Enum):
    USER = "USER"
    ADMIN = "ADMIN"
    SUPERADMIN = "SUPERADMIN"

class EmploymentType(str, enum.Enum):
    FULL_TIME = "FULL_TIME"
    INTERNSHIP = "INTERNSHIP"
    CONTRACT = "CONTRACT"
    PART_TIME = "PART_TIME"
    GRADUATE_FRESHER = "GRADUATE_FRESHER"
    APPRENTICESHIP = "APPRENTICESHIP"

class RemoteType(str, enum.Enum):
    REMOTE = "REMOTE"
    HYBRID = "HYBRID"
    ON_SITE = "ON_SITE"

class JobStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    EXPIRED = "EXPIRED"
    CLOSED = "CLOSED"
    REMOVED = "REMOVED"
    UNKNOWN = "UNKNOWN"

class ApplicationStatus(str, enum.Enum):
    APPLIED = "Applied"
    ASSESSMENT = "Assessment"
    INTERVIEW = "Interview"
    TECHNICAL_INTERVIEW = "Technical Interview"
    HR_INTERVIEW = "HR Interview"
    OFFER = "Offer"
    REJECTED = "Rejected"
    WITHDRAWN = "Withdrawn"
    NO_RESPONSE = "No Response"

class SavedCategory(str, enum.Enum):
    SAVED = "Saved"
    APPLY_LATER = "Apply Later"
    HIGH_PRIORITY = "High Priority"

class PlanTier(str, enum.Enum):
    FREE = "FREE"
    PRO_99 = "PRO_99"
    ELITE_189 = "ELITE_189"
    PREMIUM_CONFIGURABLE = "PREMIUM_CONFIGURABLE"

class SourceType(str, enum.Enum):
    OFFICIAL_COMPANY = "OFFICIAL_COMPANY"
    OFFICIAL_ATS = "OFFICIAL_ATS"
    AUTHORIZED_API = "AUTHORIZED_API"
    PUBLIC_JOB_BOARD = "PUBLIC_JOB_BOARD"
    OTHER = "OTHER"
