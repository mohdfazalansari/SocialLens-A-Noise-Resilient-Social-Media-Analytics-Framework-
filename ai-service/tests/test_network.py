import pytest
from app.schemas.network import InteractionEdge, NetworkAnalysisRequest
from app.services.network_service import network_analyzer

def test_network_analysis():
    interactions = [
        InteractionEdge(source="user_alice", target="user_bob", type="REPLY", weight=3),
        InteractionEdge(source="user_charlie", target="user_bob", type="REPLY", weight=2),
        InteractionEdge(source="user_david", target="user_bob", type="MENTION", weight=1),
        InteractionEdge(source="user_bob", target="user_alice", type="REPLY", weight=1),
    ]

    req = NetworkAnalysisRequest(interactions=interactions)
    res = network_analyzer.analyze_network(req)

    assert res.total_nodes >= 4
    assert res.total_edges >= 4
    assert res.density > 0.0

    # user_bob should have highest in-degree and top PageRank
    top_node = res.nodes[0]
    assert top_node.node_id == "user_bob"
    assert top_node.in_degree == 3
    assert top_node.pagerank > 0.0

