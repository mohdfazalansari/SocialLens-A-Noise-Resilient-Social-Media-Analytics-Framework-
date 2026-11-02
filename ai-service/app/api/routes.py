from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any

from app.schemas.preprocessing import PostRawInput, PreprocessedPost
from app.schemas.duplicates import DuplicateDetectionRequest, DuplicateDetectionResponse
from app.schemas.sentiment import BatchSentimentRequest, SentimentResult
from app.schemas.emotion import BatchEmotionRequest, EmotionResult
from app.schemas.topics import TopicDiscoveryRequest, TopicDiscoveryResponse
from app.schemas.trends import TrendDetectionRequest, TrendDetectionResponse
from app.schemas.network import NetworkAnalysisRequest, NetworkAnalysisResponse
from app.schemas.evaluation import EvaluationRequest, EvaluationResponse

from app.preprocessing.cleaner import clean_social_text
from app.services.duplicate_service import duplicate_detector
from app.services.sentiment_service import sentiment_classifier
from app.services.emotion_service import emotion_classifier
from app.services.topic_service import topic_discoverer
from app.services.trend_service import trend_engine
from app.services.network_service import network_analyzer
from app.services.evaluation_service import research_evaluator

router = APIRouter()

@router.post("/preprocess", response_model=List[PreprocessedPost])
def preprocess_posts(posts: List[PostRawInput]):
    results = []
    for p in posts:
        cleaned, hashtags, mentions, has_media = clean_social_text(p.text)
        results.append(PreprocessedPost(
            id=p.id,
            raw_text=p.text,
            cleaned_text=cleaned,
            hashtags=hashtags,
            mentions=mentions,
            has_media_link=has_media
        ))
    return results

@router.post("/detect-duplicates", response_model=DuplicateDetectionResponse)
def detect_duplicates(request: DuplicateDetectionRequest):
    return duplicate_detector.detect_duplicates(request.posts, request.similarity_threshold or 0.85)

@router.post("/sentiment", response_model=List[SentimentResult])
def batch_sentiment(request: BatchSentimentRequest):
    return sentiment_classifier.predict_batch(request.items)

@router.post("/emotions", response_model=List[EmotionResult])
def batch_emotions(request: BatchEmotionRequest):
    return emotion_classifier.predict_batch(request.items)

@router.post("/topics/discover", response_model=TopicDiscoveryResponse)
def discover_topics(request: TopicDiscoveryRequest):
    return topic_discoverer.discover_topics(request.posts, request.min_topic_size or 3)

@router.post("/trends/detect", response_model=TrendDetectionResponse)
def detect_trends(request: TrendDetectionRequest):
    return trend_engine.detect_trends(request)

@router.post("/network/analyze", response_model=NetworkAnalysisResponse)
def analyze_network(request: NetworkAnalysisRequest):
    return network_analyzer.analyze_network(request)

@router.post("/evaluation/run", response_model=EvaluationResponse)
def run_evaluation(request: EvaluationRequest):
    return research_evaluator.evaluate_methods(request)

