from pydantic import BaseModel
from typing import List, Optional
from app.schemas.preprocessing import PostRawInput

class DuplicateCluster(BaseModel):
    cluster_hash: str
    duplicate_type: str  # EXACT, NEAR_DUPLICATE
    similarity: float
    canonical_id: int
    member_ids: List[int]

class DuplicateDetectionRequest(BaseModel):
    posts: List[PostRawInput]
    similarity_threshold: Optional[float] = 0.85

class DuplicateDetectionResponse(BaseModel):
    total_posts: int
    unique_clusters_count: int
    total_duplicates_found: int
    clusters: List[DuplicateCluster]

