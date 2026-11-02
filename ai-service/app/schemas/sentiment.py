from pydantic import BaseModel
from typing import List, Optional

class TextItem(BaseModel):
    id: int
    text: str

class SentimentResult(BaseModel):
    id: int
    label: str  # POSITIVE, NEGATIVE, NEUTRAL
    positive_score: float
    negative_score: float
    neutral_score: float
    model_name: str

class BatchSentimentRequest(BaseModel):
    items: List[TextItem]

