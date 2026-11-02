import hashlib
from typing import List, Dict, Any, Set
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import networkx as nx

from app.schemas.preprocessing import PostRawInput
from app.schemas.duplicates import DuplicateCluster, DuplicateDetectionResponse
from app.preprocessing.cleaner import clean_social_text

class DuplicateDetector:

    def detect_duplicates(self, posts: List[PostRawInput], threshold: float = 0.85) -> DuplicateDetectionResponse:
        if not posts:
            return DuplicateDetectionResponse(
                total_posts=0,
                unique_clusters_count=0,
                total_duplicates_found=0,
                clusters=[]
            )

        post_map: Dict[int, PostRawInput] = {p.id: p for p in posts}
        cleaned_texts: Dict[int, str] = {}
        exact_hashes: Dict[str, List[int]] = {}

        # 1. Exact Duplicate Grouping via MD5 Hash
        for p in posts:
            cleaned, _, _, _ = clean_social_text(p.text)
            cleaned_texts[p.id] = cleaned
            norm = cleaned.lower().strip()
            md5_hash = hashlib.md5(norm.encode("utf-8")).hexdigest()
            exact_hashes.setdefault(md5_hash, []).append(p.id)

        clusters: List[DuplicateCluster] = []
        already_grouped: Set[int] = set()

        for h, ids in exact_hashes.items():
            if len(ids) > 1:
                canonical = sorted(ids)[0]
                clusters.append(DuplicateCluster(
                    cluster_hash=h,
                    duplicate_type="EXACT",
                    similarity=1.0000,
                    canonical_id=canonical,
                    member_ids=ids
                ))
                already_grouped.update(ids)

        # 2. Near-Duplicate Grouping via TF-IDF Cosine Similarity for remaining ungrouped posts
        remaining_ids = [p.id for p in posts if p.id not in already_grouped]
        remaining_texts = [cleaned_texts[pid] for pid in remaining_ids]

        if len(remaining_ids) >= 2:
            try:
                vectorizer = TfidfVectorizer(
                    ngram_range=(1, 2),
                    min_df=1,
                    stop_words="english",
                    max_features=5000
                )
                tfidf_matrix = vectorizer.fit_transform(remaining_texts)
                sim_matrix = cosine_similarity(tfidf_matrix)

                sim_graph = nx.Graph()
                for i, pid in enumerate(remaining_ids):
                    sim_graph.add_node(pid)

                n = len(remaining_ids)
                for i in range(n):
                    for j in range(i + 1, n):
                        sim_val = float(sim_matrix[i, j])
                        if sim_val >= threshold:
                            sim_graph.add_edge(remaining_ids[i], remaining_ids[j], weight=sim_val)

                components = list(nx.connected_components(sim_graph))
                for comp in components:
                    if len(comp) > 1:
                        member_list = sorted(list(comp))
                        canonical = member_list[0]
                        # Compute average pairwise similarity in component
                        subgraph = sim_graph.subgraph(comp)
                        weights = [d["weight"] for _, _, d in subgraph.edges(data=True)]
                        avg_sim = float(np.mean(weights)) if weights else threshold

                        comp_hash = hashlib.sha256("-".join(map(str, member_list)).encode("utf-8")).hexdigest()[:16]
                        clusters.append(DuplicateCluster(
                            cluster_hash=f"near_{comp_hash}",
                            duplicate_type="NEAR_DUPLICATE",
                            similarity=round(avg_sim, 4),
                            canonical_id=canonical,
                            member_ids=member_list
                        ))
                        already_grouped.update(comp)
            except Exception as e:
                # If TF-IDF vocabulary is completely empty or all stop words
                pass

        total_dups = sum(len(c.member_ids) - 1 for c in clusters)
        unique_clusters = len(clusters)

        return DuplicateDetectionResponse(
            total_posts=len(posts),
            unique_clusters_count=unique_clusters,
            total_duplicates_found=total_dups,
            clusters=clusters
        )

duplicate_detector = DuplicateDetector()

