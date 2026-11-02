# API Contracts & Specification Guide

## 1. Spring Boot REST API (`http://localhost:8080/api`)

### Authentication & User Management
- `POST /api/auth/register`: Register new user (`username`, `email`, `password`, `role`).
- `POST /api/auth/login`: Authenticate and receive JWT Bearer token.
- `GET /api/auth/me`: Retrieve authenticated user profile and roles.

### Dataset Management
- `POST /api/datasets/preview`: Multipart CSV/JSON upload to inspect columns and preview sample rows.
- `POST /api/datasets/upload`: Ingest posts with custom column mappings.
- `GET /api/datasets`: List datasets with pagination.
- `GET /api/datasets/{id}`: Dataset details, records count, platform and language distributions.
- `DELETE /api/datasets/{id}`: Delete dataset and associated analysis entities.
- `POST /api/datasets/{id}/analyze`: Trigger asynchronous AI analytical pipeline.
- `GET /api/datasets/{id}/status`: Query progress and status of active analytics job.

### Posts Explorer
- `GET /api/posts`: Query posts with pagination, text search (`query`), platform filter, duplicate filter (`isDuplicate`), and sorting.
- `GET /api/posts/{id}`: Retrieve single post with enriched RoBERTa sentiment and topic annotations.

### Analytics Endpoints
- `GET /api/analytics/overview?datasetId={id}`: Dashboard KPI metrics.
- `GET /api/analytics/sentiment?datasetId={id}`: Sentiment distribution & over-time time points.
- `GET /api/analytics/emotions?datasetId={id}`: 7-class emotion distribution.
- `GET /api/analytics/topics?datasetId={id}`: Discovered BERTopics with keywords and coherence.

### Trend Detection & Anomaly Alerts
- `GET /api/trends?datasetId={id}&method={method}`: Temporal snapshots and alerts switchable across `BASELINE_VOLUME`, `BASELINE_GROWTH`, and `PROPOSED_NOISE_RESILIENT`.
- `GET /api/trends/{alertId}`: Detailed alert dossier with supporting post record IDs.

### Social Network & Influencer Analysis
- `GET /api/network/graph?datasetId={id}&maxNodes={limit}`: Node and edge topology.
- `GET /api/network/influencers?datasetId={id}&limit={limit}`: Top accounts ranked by PageRank.

### Research Evaluation
- `POST /api/evaluation/run?datasetId={id}`: Run benchmark experiment comparing baselines vs proposed method.
- `GET /api/evaluation/runs?datasetId={id}`: Historical evaluation runs.

---

## 2. Python FastAPI AI Endpoints (`http://localhost:8000/ai`)

- `GET /health`: Health status, CUDA availability, and model initialization state.
- `POST /ai/preprocess`: Cleans text, normalizes mentions, extracts hashtags and external media links.
- `POST /ai/detect-duplicates`: Exact MD5 hashing + near-duplicate TF-IDF cosine similarity clustering.
- `POST /ai/sentiment`: Batch CardiffNLP Twitter-RoBERTa sentiment classification.
- `POST /ai/emotions`: Batch DistilRoBERTa 7-class emotion classification.
- `POST /ai/topics/discover`: Sentence-Transformers + BERTopic clustering and c-TF-IDF keyword extraction.
- `POST /ai/trends/detect`: Noise-resilient multi-signal trend calculation and anomaly detection.
- `POST /ai/network/analyze`: NetworkX PageRank, betweenness centrality, degree metrics, and community detection.
- `POST /ai/evaluation/run`: Precision, recall, F1-score, and false-alarm rate comparative evaluation.

