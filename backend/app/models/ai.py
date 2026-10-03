from sqlalchemy import Column, ForeignKey, String, Text
from app.models.base import TimeStampedModel

class AIConversation(TimeStampedModel):
    __tablename__ = "ai_conversations"

    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    session_id = Column(String(100), nullable=False, index=True)
    user_prompt = Column(Text, nullable=False)
    extracted_filters = Column(Text, nullable=True) # JSON structured representation
    assistant_response = Column(Text, nullable=False)
