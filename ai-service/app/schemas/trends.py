from pydantic import BaseModel
from typing import List, Optional, Dict, Any

class TrendPostItem(BaseModel):
    id: int
    author_id: Optional[str] = None
    created_at: str
    is_duplicate: Optional[bool] = False
    likes: Optional[int] = 0
    replies: Optional[int] = 0
    shares: Optional[int] = 0
    sentiment: Optional[str] = "NEUTRAL"
    sentiment_pos: Optional[float] = 0.33
    sentiment_neg: Optional[float] = 0.33
    topic_index: int

class TrendDetectionRequest(BaseModel):
    dataset_id: int
    posts: List[TrendPostItem]
    alpha: Optional[float] = 0.40  # Weight for Z-Score anomaly
    beta: Optional[float] = 0.30   # Weight for unique Growth Rate
    gamma: Optional[float] = 0.20  # Weight for Sentiment Shift
    delta: Optional[float] = 0.10  # Weight for Engagement
    window_size_hours: Optional[int] = 24

class TrendSnapshotItem(BaseModel):
    topic_index: int
    window_start: str
    window_end: str
    raw_post_count: int
    unique_post_count: int
    duplicate_ratio: float
    growth_rate: float
    z_score: float
    sentiment_shift: float
    trend_score: float
    classification: str  # EMERGING, RISING, STABLE, DECLINING

class TrendAlertItem(BaseModel):
    topic_index: int
    alert_level: str  # HIGH, MEDIUM, LOW
    classification: str
    trend_score: float
    baseline_volume: float
    current_volume: float
    growth_percentage: float
    sentiment_shift: float
    duplicate_ratio: float
    alert_reason: str
    supporting_post_ids: List[int]

class TrendDetectionResponse(BaseModel):
    dataset_id: int
    snapshots: List[TrendSnapshotItem]
    alerts: List[TrendAlertItem]

