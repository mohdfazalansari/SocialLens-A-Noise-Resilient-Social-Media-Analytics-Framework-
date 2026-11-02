from pydantic import BaseModel
from typing import List, Optional
from app.schemas.preprocessing import PostRawInput

class TopicItem(BaseModel):
    topic_index: int
    name: str
    keywords: str
    count: int
    coherence: float

class TopicAssignment(BaseModel):
    post_id: int
    topic_index: int
    probability: float

class TopicDiscoveryRequest(BaseModel):
    posts: List[PostRawInput]
    min_topic_size: Optional[int] = 3

class TopicDiscoveryResponse(BaseModel):
    total_topics: int
    topics: List[TopicItem]
    assignments: List[TopicAssignment]

