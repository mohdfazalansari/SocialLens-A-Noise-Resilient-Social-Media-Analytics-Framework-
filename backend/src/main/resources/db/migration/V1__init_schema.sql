-- ========================================================
-- Schema Migration: V1__init_schema.sql
-- Project: SocialLens - Social Media Analytics Framework
-- ========================================================

-- 1. USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ANALYST',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. DATASETS
CREATE TABLE IF NOT EXISTS datasets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_format VARCHAR(20) NOT NULL DEFAULT 'CSV',
    storage_path VARCHAR(500),
    total_records INT DEFAULT 0,
    analysis_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_datasets_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_datasets_status ON datasets(analysis_status);

-- 3. DUPLICATE GROUPS
CREATE TABLE IF NOT EXISTS duplicate_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_hash VARCHAR(64) NOT NULL UNIQUE,
    duplicate_type VARCHAR(30) NOT NULL DEFAULT 'EXACT',
    similarity_score DECIMAL(5, 4) DEFAULT 1.0000,
    canonical_post_id BIGINT,
    member_count INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_dup_hash ON duplicate_groups(group_hash);

-- 4. SOCIAL POSTS
CREATE TABLE IF NOT EXISTS social_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    platform_post_id VARCHAR(100),
    platform VARCHAR(50) DEFAULT 'TWITTER',
    author_id VARCHAR(100),
    post_text TEXT NOT NULL,
    cleaned_text TEXT,
    language VARCHAR(20) DEFAULT 'en',
    like_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    reply_to_post_id VARCHAR(100),
    is_duplicate BOOLEAN DEFAULT FALSE,
    duplicate_group_id BIGINT,
    post_created_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_dupgroup FOREIGN KEY (duplicate_group_id) REFERENCES duplicate_groups(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_posts_dataset ON social_posts(dataset_id);
CREATE INDEX idx_posts_author ON social_posts(author_id);
CREATE INDEX idx_posts_created_at ON social_posts(post_created_at);
CREATE INDEX idx_posts_platform ON social_posts(platform);
CREATE INDEX idx_posts_is_dup ON social_posts(is_duplicate);

-- 5. SENTIMENT RESULTS
CREATE TABLE IF NOT EXISTS sentiment_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL UNIQUE,
    label VARCHAR(30) NOT NULL,
    positive_score DECIMAL(5, 4) NOT NULL,
    negative_score DECIMAL(5, 4) NOT NULL,
    neutral_score DECIMAL(5, 4) NOT NULL,
    model_name VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sentiment_post FOREIGN KEY (post_id) REFERENCES social_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sentiment_label ON sentiment_results(label);

-- 6. EMOTION RESULTS
CREATE TABLE IF NOT EXISTS emotion_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL UNIQUE,
    dominant_emotion VARCHAR(30) NOT NULL,
    emotion_confidence DECIMAL(5, 4) NOT NULL,
    emotion_scores JSON,
    model_name VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emotion_post FOREIGN KEY (post_id) REFERENCES social_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_emotion_dominant ON emotion_results(dominant_emotion);

-- 7. TOPICS
CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    topic_index INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    top_keywords TEXT,
    post_count INT DEFAULT 0,
    representative_coherence DECIMAL(5, 4) DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_topics_dataset ON topics(dataset_id);

-- 8. POST TOPICS (Many-to-Many Relationship with Weights)
CREATE TABLE IF NOT EXISTS post_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    probability DECIMAL(5, 4) DEFAULT 1.0000,
    CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES social_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT uq_post_topic UNIQUE (post_id, topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. TREND SNAPSHOTS
CREATE TABLE IF NOT EXISTS trend_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    raw_post_count INT DEFAULT 0,
    unique_post_count INT DEFAULT 0,
    duplicate_ratio DECIMAL(5, 4) DEFAULT 0.0000,
    growth_rate DECIMAL(7, 4) DEFAULT 0.0000,
    z_score DECIMAL(7, 4) DEFAULT 0.0000,
    sentiment_shift DECIMAL(7, 4) DEFAULT 0.0000,
    trend_score DECIMAL(7, 4) DEFAULT 0.0000,
    classification VARCHAR(30) NOT NULL DEFAULT 'STABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_snapshots_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE,
    CONSTRAINT fk_snapshots_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_snapshots_topic ON trend_snapshots(topic_id);
CREATE INDEX idx_snapshots_window ON trend_snapshots(window_start, window_end);

-- 10. TREND ALERTS
CREATE TABLE IF NOT EXISTS trend_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    alert_level VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    classification VARCHAR(30) NOT NULL DEFAULT 'EMERGING',
    trend_score DECIMAL(7, 4) NOT NULL,
    baseline_volume DECIMAL(7, 2) DEFAULT 0.00,
    current_volume DECIMAL(7, 2) DEFAULT 0.00,
    growth_percentage DECIMAL(7, 2) DEFAULT 0.00,
    sentiment_shift DECIMAL(7, 4) DEFAULT 0.0000,
    duplicate_ratio DECIMAL(5, 4) DEFAULT 0.0000,
    alert_reason TEXT,
    supporting_post_ids JSON,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerts_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE,
    CONSTRAINT fk_alerts_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_alerts_dataset ON trend_alerts(dataset_id);
CREATE INDEX idx_alerts_level ON trend_alerts(alert_level);

-- 11. NETWORK NODES
CREATE TABLE IF NOT EXISTS network_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    node_id VARCHAR(100) NOT NULL,
    pseudonym VARCHAR(100) NOT NULL,
    in_degree INT DEFAULT 0,
    out_degree INT DEFAULT 0,
    betweenness_centrality DECIMAL(7, 6) DEFAULT 0.000000,
    pagerank DECIMAL(7, 6) DEFAULT 0.000000,
    community_id INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nodes_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE,
    CONSTRAINT uq_dataset_node UNIQUE (dataset_id, node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_nodes_dataset ON network_nodes(dataset_id);
CREATE INDEX idx_nodes_pagerank ON network_nodes(pagerank);

-- 12. NETWORK EDGES
CREATE TABLE IF NOT EXISTS network_edges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    source_node VARCHAR(100) NOT NULL,
    target_node VARCHAR(100) NOT NULL,
    interaction_type VARCHAR(30) NOT NULL DEFAULT 'REPLY',
    weight INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_edges_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_edges_dataset ON network_edges(dataset_id);

-- 13. ANALYTICS JOBS
CREATE TABLE IF NOT EXISTS analytics_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    job_type VARCHAR(50) NOT NULL DEFAULT 'FULL_PIPELINE',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    progress_percentage INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_jobs_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_jobs_dataset ON analytics_jobs(dataset_id);
CREATE INDEX idx_jobs_status ON analytics_jobs(status);

-- 14. EVALUATION RUNS
CREATE TABLE IF NOT EXISTS evaluation_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    ground_truth_source VARCHAR(255),
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. EVALUATION RESULTS
CREATE TABLE IF NOT EXISTS evaluation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id BIGINT NOT NULL,
    method_name VARCHAR(50) NOT NULL,
    precision_score DECIMAL(5, 4) DEFAULT 0.0000,
    recall_score DECIMAL(5, 4) DEFAULT 0.0000,
    f1_score DECIMAL(5, 4) DEFAULT 0.0000,
    false_alert_rate DECIMAL(5, 4) DEFAULT 0.0000,
    avg_detection_delay_hours DECIMAL(7, 2) DEFAULT 0.00,
    metrics_json TEXT,
    CONSTRAINT fk_results_run FOREIGN KEY (run_id) REFERENCES evaluation_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_results_run ON evaluation_results(run_id);

