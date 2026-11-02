from pydantic import BaseModel
from typing import List, Optional

class PostRawInput(BaseModel):
    id: int
    text: str
    author_id: Optional[str] = None
    created_at: Optional[str] = None
    like_count: Optional[int] = 0
    reply_count: Optional[int] = 0
    share_count: Optional[int] = 0
    reply_to_post_id: Optional[str] = None

class PreprocessedPost(BaseModel):
    id: int
    raw_text: str
    cleaned_text: str
    hashtags: List[str] = []
    mentions: List[str] = []
    has_media_link: bool = False

