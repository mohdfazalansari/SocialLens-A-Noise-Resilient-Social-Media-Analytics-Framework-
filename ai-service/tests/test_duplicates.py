import pytest
from app.schemas.preprocessing import PostRawInput
from app.services.duplicate_service import duplicate_detector

def test_exact_duplicate_detection():
    posts = [
        PostRawInput(id=1, text="The phone battery drains too fast!"),
        PostRawInput(id=2, text="The phone battery drains too fast!"),
        PostRawInput(id=3, text="Camera quality is super impressive!"),
    ]
    res = duplicate_detector.detect_duplicates(posts, threshold=0.85)

    assert res.total_posts == 3
    assert res.total_duplicates_found >= 1
    assert len(res.clusters) >= 1
    assert res.clusters[0].duplicate_type == "EXACT"
    assert 1 in res.clusters[0].member_ids
    assert 2 in res.clusters[0].member_ids

def test_near_duplicate_detection():
    posts = [
        PostRawInput(id=10, text="The battery drains extremely quickly on this new phone"),
        PostRawInput(id=11, text="The battery drains very quickly on this new phone model"),
        PostRawInput(id=12, text="Display screen colors look brilliant and sharp"),
    ]
    res = duplicate_detector.detect_duplicates(posts, threshold=0.60)

    assert res.total_posts == 3
    assert len(res.clusters) >= 1
    near_dups = [c for c in res.clusters if c.duplicate_type == "NEAR_DUPLICATE"]
    assert len(near_dups) >= 1

