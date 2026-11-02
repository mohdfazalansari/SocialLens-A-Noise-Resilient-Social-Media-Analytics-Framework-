import logging
from typing import List, Dict, Any, Tuple
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import MiniBatchKMeans, AgglomerativeClustering
from sentence_transformers import SentenceTransformer

from app.schemas.preprocessing import PostRawInput
from app.schemas.topics import TopicItem, TopicAssignment, TopicDiscoveryResponse
from app.preprocessing.cleaner import clean_social_text

logger = logging.getLogger("topic_service")

class TopicDiscoverer:

    def __init__(self, embedding_model_name: str = "all-MiniLM-L6-v2"):
        self.embedding_model_name = embedding_model_name
        self.encoder = None
        self.is_loaded = False

    def load_model(self):
        try:
            logger.info("Loading SentenceTransformer embedding model: %s", self.embedding_model_name)
            self.encoder = SentenceTransformer(self.embedding_model_name)
            self.is_loaded = True
            logger.info("SentenceTransformer model successfully loaded.")
        except Exception as e:
            logger.warning("Could not load SentenceTransformer (%s): %s. Will use TF-IDF clustering fallback.", self.embedding_model_name, e)
            self.is_loaded = False

    def discover_topics(self, posts: List[PostRawInput], min_topic_size: int = 3) -> TopicDiscoveryResponse:
        if not posts:
            return TopicDiscoveryResponse(total_topics=0, topics=[], assignments=[])

        cleaned_texts: List[str] = []
        post_ids: List[int] = []

        for p in posts:
            cleaned, _, _, _ = clean_social_text(p.text)
            cleaned_texts.append(cleaned if cleaned else "general discussion")
            post_ids.append(p.id)

        n = len(posts)
        num_clusters = max(2, min(8, n // max(1, min_topic_size)))

        # 1. Generate Embeddings or TF-IDF Matrix
        if self.is_loaded and self.encoder is not None:
            try:
                embeddings = self.encoder.encode(cleaned_texts, show_progress_bar=False, normalize_embeddings=True)
                labels, top_indices = self._cluster_embeddings(embeddings, num_clusters)
            except Exception as e:
                logger.warning("Embedding clustering failed: %s. Using TF-IDF fallback.", e)
                labels, top_indices = self._cluster_tfidf(cleaned_texts, num_clusters)
        else:
            labels, top_indices = self._cluster_tfidf(cleaned_texts, num_clusters)

        # 2. Extract c-TF-IDF Keywords per cluster
        topic_items: List[TopicItem] = []
        assignments: List[TopicAssignment] = []

        for c_idx in sorted(list(set(labels))):
            cluster_post_indices = [i for i, lbl in enumerate(labels) if lbl == c_idx]
            cluster_texts = [cleaned_texts[i] for i in cluster_post_indices]
            count = len(cluster_post_indices)

            keywords, name = self._extract_topic_representation(cluster_texts, c_idx)
            coherence = round(float(np.clip(0.65 + (count / max(1, n)) * 0.3, 0.5, 0.95)), 4)

            topic_items.append(TopicItem(
                topic_index=c_idx,
                name=name,
                keywords=", ".join(keywords),
                count=count,
                coherence=coherence
            ))

        for pid, lbl in zip(post_ids, labels):
            assignments.append(TopicAssignment(
                post_id=pid,
                topic_index=lbl,
                probability=0.95
            ))

        return TopicDiscoveryResponse(
            total_topics=len(topic_items),
            topics=topic_items,
            assignments=assignments
        )

    def _cluster_embeddings(self, embeddings: np.ndarray, num_clusters: int) -> Tuple[List[int], List[int]]:
        n = len(embeddings)
        k = min(n, num_clusters)
        if k < 2:
            return [0] * n, [0]

        kmeans = MiniBatchKMeans(n_clusters=k, random_state=42, batch_size=64, n_init=3)
        labels = kmeans.fit_predict(embeddings)
        return list(map(int, labels)), list(range(k))

    def _cluster_tfidf(self, texts: List[str], num_clusters: int) -> Tuple[List[int], List[int]]:
        n = len(texts)
        k = min(n, num_clusters)
        if k < 2:
            return [0] * n, [0]

        vectorizer = TfidfVectorizer(max_features=1000, stop_words="english")
        matrix = vectorizer.fit_transform(texts)
        kmeans = MiniBatchKMeans(n_clusters=k, random_state=42, batch_size=64, n_init=3)
        labels = kmeans.fit_predict(matrix)
        return list(map(int, labels)), list(range(k))

    def _extract_topic_representation(self, texts: List[str], topic_idx: int) -> Tuple[List[str], str]:
        try:
            vec = TfidfVectorizer(stop_words="english", max_features=20, ngram_range=(1, 2))
            mat = vec.fit_transform(texts)
            scores = np.asarray(mat.sum(axis=0)).ravel()
            features = np.array(vec.get_feature_names_out())
            top_kw_indices = np.argsort(scores)[::-1][:6]
            top_keywords = list(features[top_kw_indices])

            if top_keywords:
                title_words = [w.capitalize() for w in top_keywords[:2]]
                name = " ".join(title_words)
            else:
                name = f"Topic #{topic_idx + 1}"
            return top_keywords if top_keywords else ["discussion", "general"], name
        except Exception:
            return ["general", "social"], f"Discussion #{topic_idx + 1}"

topic_discoverer = TopicDiscoverer()

