from typing import Optional
from sqlalchemy.orm import Session
from app.models.user import AuditLog

class AuditRepository:
    def __init__(self, db: Session):
        self.db = db

    def log(
        self,
        action: str,
        resource_type: str,
        resource_id: Optional[str] = None,
        actor_id: Optional[str] = None,
        details: Optional[str] = None,
        ip_address: Optional[str] = None
    ) -> AuditLog:
        audit_log = AuditLog(
            actor_id=actor_id,
            action=action,
            resource_type=resource_type,
            resource_id=resource_id,
            details=details,
            ip_address=ip_address
        )
        self.db.add(audit_log)
        self.db.commit()
        return audit_log
