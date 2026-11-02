import re
import unicodedata
from typing import Tuple, List

URL_PATTERN = re.compile(r'https?://\S+|www\.\S+')
MENTION_PATTERN = re.compile(r'@([a-zA-Z0-9_]{1,50})')
HASHTAG_PATTERN = re.compile(r'#([a-zA-Z0-9_]+)')
WHITESPACE_PATTERN = re.compile(r'\s+')
EMOJI_PATTERN = re.compile(
    r'[\U0001F600-\U0001F64F]'
    r'|[\U0001F300-\U0001F5FF]'
    r'|[\U0001F680-\U0001F6FF]'
    r'|[\U0001F1E0-\U0001F1FF]'
    r'|[\U00002702-\U000027B0]'
    r'|[\U0001F900-\U0001F9FF]'
    r'|[\U0001FA70-\U0001FAFF]',
    flags=re.UNICODE
)

def clean_social_text(text: str) -> Tuple[str, List[str], List[str], bool]:
    """
    Cleans raw social media text:
    - Normalizes Unicode
    - Identifies & extracts hashtags
    - Identifies & extracts mentions
    - Detects external/media links and normalizes them
    - Trims extraneous whitespace
    Returns: (cleaned_text, hashtags, mentions, has_media_link)
    """
    if not text:
        return "", [], [], False

    # Unicode normalization
    normalized = unicodedata.normalize("NFKC", str(text))

    # Extract hashtags
    hashtags = HASHTAG_PATTERN.findall(normalized)

    # Extract mentions
    mentions = MENTION_PATTERN.findall(normalized)

    # Detect URLs
    urls = URL_PATTERN.findall(normalized)
    has_media_link = len(urls) > 0

    # Replace URLs with normalized token
    cleaned = URL_PATTERN.sub(" http_url ", normalized)

    # Replace mentions with normalized token
    cleaned = MENTION_PATTERN.sub(" @user ", cleaned)

    # Emoji normalization: keep meaningful spaces around emojis
    cleaned = EMOJI_PATTERN.sub(lambda m: f" {m.group(0)} ", cleaned)

    # Whitespace compression
    cleaned = WHITESPACE_PATTERN.sub(" ", cleaned).strip()

    return cleaned, hashtags, mentions, has_media_link
