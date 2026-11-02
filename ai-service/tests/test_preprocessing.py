import pytest
from app.preprocessing.cleaner import clean_social_text

def test_clean_social_text_basic():
    raw = "Check out this amazing battery life! #Tech #Battery https://example.com/phone @elonmusk"
    cleaned, hashtags, mentions, has_media = clean_social_text(raw)

    assert "Check out this amazing battery life!" in cleaned
    assert "http_url" in cleaned
    assert "@user" in cleaned
    assert "Tech" in hashtags
    assert "Battery" in hashtags
    assert "elonmusk" in mentions
    assert has_media is True

def test_emoji_and_whitespace():
    raw = "Great   service!   🔥🚀  Loving it. "
    cleaned, _, _, _ = clean_social_text(raw)

    assert "Great service!" in cleaned
    assert "Loving it." in cleaned
    assert "🔥" in cleaned

