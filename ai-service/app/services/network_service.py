import logging
from typing import List, Dict, Any
import networkx as nx

from app.schemas.network import (
    InteractionEdge,
    NodeMetric,
    EdgeMetric,
    NetworkAnalysisRequest,
    NetworkAnalysisResponse
)

logger = logging.getLogger("network_service")

class NetworkAnalyzer:

    def analyze_network(self, request: NetworkAnalysisRequest) -> NetworkAnalysisResponse:
        interactions = request.interactions
        if not interactions:
            return NetworkAnalysisResponse(
                total_nodes=0,
                total_edges=0,
                density=0.0,
                connected_components=0,
                nodes=[],
                edges=[]
            )

        G = nx.DiGraph()

        # Build directed graph with edge weights
        for edge in interactions:
            u, v = str(edge.source), str(edge.target)
            if not u or not v or u == v:
                continue
            if G.has_edge(u, v):
                G[u][v]["weight"] += edge.weight or 1
            else:
                G.add_edge(u, v, weight=edge.weight or 1, type=edge.type or "REPLY")

        total_nodes = G.number_of_nodes()
        total_edges = G.number_of_edges()

        if total_nodes == 0:
            return NetworkAnalysisResponse(
                total_nodes=0,
                total_edges=0,
                density=0.0,
                connected_components=0,
                nodes=[],
                edges=[]
            )

        # 1. PageRank
        try:
            pagerank = nx.pagerank(G, alpha=0.85, max_iter=100)
        except Exception:
            pagerank = {node: 1.0 / total_nodes for node in G.nodes()}

        # 2. Betweenness Centrality
        try:
            betweenness = nx.betweenness_centrality(G, normalized=True)
        except Exception:
            betweenness = {node: 0.0 for node in G.nodes()}

        # 3. Weakly connected components
        num_components = nx.number_weakly_connected_components(G)

        # 4. Community detection via modularity on undirected projection
        try:
            undirected_G = G.to_undirected()
            communities = list(nx.community.greedy_modularity_communities(undirected_G))
            community_map = {}
            for comm_id, comm_nodes in enumerate(communities):
                for node in comm_nodes:
                    community_map[node] = comm_id
        except Exception:
            community_map = {node: 0 for node in G.nodes()}

        node_metrics: List[NodeMetric] = []
        for node in G.nodes():
            in_deg = G.in_degree(node)
            out_deg = G.out_degree(node)
            pr = round(float(pagerank.get(node, 0.0)), 6)
            bc = round(float(betweenness.get(node, 0.0)), 6)
            comm = community_map.get(node, 0)

            node_metrics.append(NodeMetric(
                node_id=node,
                pseudonym=f"user_{node[:8]}",
                in_degree=in_deg,
                out_degree=out_deg,
                betweenness=bc,
                pagerank=pr,
                community=comm
            ))

        # Sort by PageRank descending
        node_metrics.sort(key=lambda n: n.pagerank, reverse=True)

        edge_metrics: List[EdgeMetric] = []
        for u, v, data in G.edges(data=True):
            edge_metrics.append(EdgeMetric(
                source=u,
                target=v,
                type=data.get("type", "REPLY"),
                weight=data.get("weight", 1)
            ))

        density = round(float(nx.density(G)), 6)

        return NetworkAnalysisResponse(
            total_nodes=total_nodes,
            total_edges=total_edges,
            density=density,
            connected_components=num_components,
            nodes=node_metrics,
            edges=edge_metrics
        )

network_analyzer = NetworkAnalyzer()

