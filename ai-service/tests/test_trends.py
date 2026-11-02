import pytest
from datetime import datetime, timedelta
from app.schemas.trends import TrendPostItem, TrendDetectionRequest
from app.services.trend_service import trend_engine

def test_trend_detection():
    now = datetime.now()
    posts = []

    # Window 1: normal volume (2 posts)
    for i in range(2):
        posts.append(TrendPostItem(
            id=i + 1,
            created_at=(now - timedelta(hours=48) + timedelta(minutes=i * 10)).isoformat(),
            topic_index=0,
            sentiment="NEUTRAL"
        ))

    # Window 2: spike in unique posts (10 posts)
    for i in range(10):
        posts.append(TrendPostItem(
            id=10 + i,
            created_at=(now - timedelta(hours=2) + timedelta(minutes=i * 5)).isoformat(),
            topic_index=0,
            sentiment="POSITIVE",
            sentiment_pos=0.85,
            sentiment_neg=0.05
        ))

    req = TrendDetectionRequest(
        dataset_id=1,
        posts=posts,
        alpha=0.40,
        beta=0.30,
        gamma=0.20,
        delta=0.10,
        window_size_hours=24
    )

    res = trend_engine.detect_trends(req)

    assert res.dataset_id == 1
    assert len(res.snapshots) > 0
    # There should be an emerging or rising alert for topic 0
    assert len(res.alerts) > 0
    assert res.alerts[0].topic_index == 0
    assert res.alerts[0].growth_percentage > 0

