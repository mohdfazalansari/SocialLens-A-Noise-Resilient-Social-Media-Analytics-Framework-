from pydantic import BaseModel
from typing import List, Optional

class InteractionEdge(BaseModel):
    source: str
    target: str
    type: Optional[str] = "REPLY"  # REPLY, MENTION, RETWEET
    weight: Optional[int] = 1

class NodeMetric(BaseModel):
    node_id: str
    pseudonym: str
    in_degree: int
    out_degree: int
    betweenness: float
    pagerank: float
    community: int

class EdgeMetric(BaseModel):
    source: str
    target: str
    type: str
    weight: int

class NetworkAnalysisRequest(BaseModel):
    interactions: List[InteractionEdge]

class NetworkAnalysisResponse(BaseModel):
    total_nodes: int
    total_edges: int
    density: float
    connected_components: int
    nodes: List[NodeMetric]
    edges: List[EdgeMetric]

