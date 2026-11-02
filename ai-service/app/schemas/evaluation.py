from pydantic import BaseModel
from typing import List, Optional, Dict, Any

class MethodEvaluationMetric(BaseModel):
    method_name: str
    precision: float
    recall: float
    f1_score: float
    false_alert_rate: float
    avg_detection_delay_hours: float
    details: Optional[Dict[str, Any]] = None

class EvaluationRequest(BaseModel):
    dataset_id: int
    snapshots_count: Optional[int] = 0
    alerts_count: Optional[int] = 0
    snapshots: Optional[List[Dict[str, Any]]] = []

class EvaluationResponse(BaseModel):
    dataset_id: int
    results: List[MethodEvaluationMetric]

