from sqlalchemy import Boolean, Column, ForeignKey, String, Text
from sqlalchemy.orm import relationship
from app.models.base import TimeStampedModel

class Notification(TimeStampedModel):
    __tablename__ = "notifications"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String(200), nullable=False)
    message = Column(Text, nullable=False)
    notification_type = Column(String(50), nullable=False) # NEW_MATCH, DEADLINE_ALERT, STATUS_CHANGE, SUBSCRIPTION
    is_read = Column(Boolean, default=False, nullable=False)
    action_url = Column(String(500), nullable=True)

    user = relationship("User", back_populates="notifications")
