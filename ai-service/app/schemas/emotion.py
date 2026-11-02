from pydantic import BaseModel
from typing import List, Dict, Optional
from app.schemas.sentiment import TextItem

class EmotionResult(BaseModel):
    id: int
    dominant_emotion: str  # joy, anger, sadness, fear, surprise, disgust, neutral
    confidence: float
    scores: Dict[str, float]
    model_name: str

class BatchEmotionRequest(BaseModel):
    items: List[TextItem]

