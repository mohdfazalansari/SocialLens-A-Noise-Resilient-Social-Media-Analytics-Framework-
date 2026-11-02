import logging
from typing import List, Dict, Any, Tuple
from datetime import datetime, timedelta
import numpy as np
import pandas as pd

from app.schemas.trends import (
    TrendPostItem,
    TrendDetectionRequest,
    TrendSnapshotItem,
    TrendAlertItem,
    TrendDetectionResponse
)

logger = logging.getLogger("trend_service")

class TrendEngine:

    def detect_trends(self, request: TrendDetectionRequest) -> TrendDetectionResponse:
        posts = request.posts
        if not posts:
            return TrendDetectionResponse(dataset_id=request.dataset_id, snapshots=[], alerts=[])

        alpha = request.alpha or 0.40
        beta = request.beta or 0.30
        gamma = request.gamma or 0.20
        delta = request.delta or 0.10
        window_hours = request.window_size_hours or 24

        # Convert to pandas DataFrame for clean temporal window slicing
        records = []
        for p in posts:
            try:
                dt = pd.to_datetime(p.created_at)
            except Exception:
                dt = pd.Timestamp.now()
            records.append({
                "id": p.id,
                "created_at": dt,
                "is_duplicate": bool(p.is_duplicate),
                "likes": int(p.likes or 0),
                "replies": int(p.replies or 0),
                "shares": int(p.shares or 0),
                "sentiment": str(p.sentiment or "NEUTRAL"),
                "sentiment_pos": float(p.sentiment_pos or 0.33),
                "sentiment_neg": float(p.sentiment_neg or 0.33),
                "topic_index": int(p.topic_index)
            })

        df = pd.DataFrame(records)
        df.sort_values(by="created_at", inplace=True)

        min_time = df["created_at"].min()
        max_time = df["created_at"].max()
        time_span_hours = max(1.0, (max_time - min_time).total_seconds() / 3600.0)

        # Dynamic window calculation: at least 3 temporal windows
        effective_window_hours = max(1, min(window_hours, int(time_span_hours / 3) if time_span_hours >= 6 else 1))

        # Bucket into time windows
        window_delta = pd.Timedelta(hours=effective_window_hours)
        windows = []
        cur_start = min_time
        while cur_start < max_time:
            cur_end = cur_start + window_delta
            windows.append((cur_start, cur_end))
            cur_start = cur_end

        if not windows:
            windows.append((min_time, max_time + pd.Timedelta(hours=1)))

        snapshots: List[TrendSnapshotItem] = []
        alerts: List[TrendAlertItem] = []

        distinct_topics = [t for t in df["topic_index"].unique() if t >= 0]

        for topic in distinct_topics:
            topic_df = df[df["topic_index"] == topic]
            topic_snapshots = []

            for w_idx, (w_start, w_end) in enumerate(windows):
                w_posts = topic_df[(topic_df["created_at"] >= w_start) & (topic_df["created_at"] < w_end)]
                raw_vol = len(w_posts)
                if raw_vol == 0:
                    continue

                dup_count = int(w_posts["is_duplicate"].sum())
                uniq_vol = max(0, raw_vol - dup_count)
                dup_ratio = round(dup_count / raw_vol, 4) if raw_vol > 0 else 0.0

                # Compute baseline from previous windows
                prev_windows_posts = topic_df[topic_df["created_at"] < w_start]
                if len(prev_windows_posts) > 0:
                    # Rolling window baseline stats
                    window_counts = []
                    for pw_start, pw_end in windows[:w_idx]:
                        pw_cnt = len(topic_df[(topic_df["created_at"] >= pw_start) & (topic_df["created_at"] < pw_end)])
                        window_counts.append(pw_cnt)
                    
                    mean_base = float(np.mean(window_counts)) if window_counts else 1.0
                    std_base = float(np.std(window_counts)) if len(window_counts) > 1 and np.std(window_counts) > 0 else 1.0
                    prev_vol = window_counts[-1] if window_counts else mean_base
                else:
                    mean_base = max(1.0, uniq_vol / 2.0)
                    std_base = 1.0
                    prev_vol = mean_base

                # 1. Growth Rate
                growth_rate = round((uniq_vol - prev_vol) / max(1.0, prev_vol), 4)

                # 2. Z-Score Anomaly (Noise-Resilient: computed on UNIQUE volume)
                z_score = round((uniq_vol - mean_base) / max(1.0, std_base), 4)

                # 3. Sentiment Shift
                pos_count = len(w_posts[w_posts["sentiment"] == "POSITIVE"])
                neg_count = len(w_posts[w_posts["sentiment"] == "NEGATIVE"])
                current_polarity = (pos_count - neg_count) / max(1, pos_count + neg_count)

                if len(prev_windows_posts) > 0:
                    prev_pos = len(prev_windows_posts[prev_windows_posts["sentiment"] == "POSITIVE"])
                    prev_neg = len(prev_windows_posts[prev_windows_posts["sentiment"] == "NEGATIVE"])
                    base_polarity = (prev_pos - prev_neg) / max(1, prev_pos + prev_neg)
                else:
                    base_polarity = 0.0

                sentiment_shift = round(abs(current_polarity - base_polarity), 4)

                # 4. Normalized Engagement
                total_engagement = int(w_posts["likes"].sum() + w_posts["replies"].sum() + w_posts["shares"].sum())
                norm_eng = float(np.log1p(total_engagement) / 10.0)

                # 5. Composite Proposed Trend Score
                # Sigmoid scaling on Z-Score
                z_sigmoid = float(1.0 / (1.0 + np.exp(-np.clip(z_score, -5.0, 5.0))))
                growth_clipped = float(np.clip(growth_rate, -1.0, 3.0))

                trend_score = round(
                    alpha * z_sigmoid +
                    beta * (growth_clipped / 3.0) +
                    gamma * sentiment_shift +
                    delta * min(1.0, norm_eng),
                    4
                )

                # Classification Logic
                if z_score >= 1.8 and growth_rate >= 0.50 and dup_ratio < 0.50:
                    classification = "EMERGING"
                elif growth_rate >= 0.25 and z_score >= 0.8:
                    classification = "RISING"
                elif growth_rate <= -0.25 or z_score <= -1.0:
                    classification = "DECLINING"
                else:
                    classification = "STABLE"

                snap = TrendSnapshotItem(
                    topic_index=topic,
                    window_start=w_start.isoformat(),
                    window_end=w_end.isoformat(),
                    raw_post_count=raw_vol,
                    unique_post_count=uniq_vol,
                    duplicate_ratio=dup_ratio,
                    growth_rate=growth_rate,
                    z_score=z_score,
                    sentiment_shift=sentiment_shift,
                    trend_score=trend_score,
                    classification=classification
                )
                topic_snapshots.append(snap)
                snapshots.append(snap)

                # Check if an Alert should be raised
                if classification in ["EMERGING", "RISING"] and uniq_vol >= 3:
                    alert_lvl = "HIGH" if classification == "EMERGING" and z_score >= 2.5 else "MEDIUM"
                    post_ids = list(w_posts["id"].astype(int))

                    reason = (
                        f"Topic #{topic + 1} exhibited {round(growth_rate * 100, 1)}% unique volume growth "
                        f"(Z-Score: {z_score}, unique posts: {uniq_vol}, duplicate spam dampening: {round(dup_ratio * 100, 1)}%). "
                        f"Sentiment divergence shifted by {round(sentiment_shift, 3)}."
                    )

                    alerts.append(TrendAlertItem(
                        topic_index=topic,
                        alert_level=alert_lvl,
                        classification=classification,
                        trend_score=trend_score,
                        baseline_volume=round(mean_base, 2),
                        current_volume=float(uniq_vol),
                        growth_percentage=round(growth_rate * 100, 2),
                        sentiment_shift=sentiment_shift,
                        duplicate_ratio=dup_ratio,
                        alert_reason=reason,
                        supporting_post_ids=post_ids[:20]
                    ))

        # Sort alerts by trend score descending
        alerts.sort(key=lambda a: a.trend_score, reverse=True)

        return TrendDetectionResponse(
            dataset_id=request.dataset_id,
            snapshots=snapshots,
            alerts=alerts
        )

trend_engine = TrendEngine()

