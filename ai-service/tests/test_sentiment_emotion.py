import pytest
from app.schemas.sentiment import TextItem
from app.services.sentiment_service import sentiment_classifier
from app.services.emotion_service import emotion_classifier

def test_sentiment_classification():
    items = [
        TextItem(id=1, text="This product is absolutely wonderful and great!"),
        TextItem(id=2, text="Terrible experience, horrible bug, worst battery drain ever."),
        TextItem(id=3, text="The system update was installed at 10 AM.")
    ]
    results = sentiment_classifier.predict_batch(items)

    assert len(results) == 3
    assert results[0].label in ["POSITIVE", "NEUTRAL", "NEGATIVE"]
    assert results[0].positive_score >= 0.0
    assert results[1].negative_score >= 0.0

def test_emotion_classification():
    items = [
        TextItem(id=1, text="I am so happy and delighted with the win!"),
        TextItem(id=2, text="I am furious, angry, and outraged by this scam!"),
    ]
    results = emotion_classifier.predict_batch(items)

    assert len(results) == 2
    assert results[0].dominant_emotion in ["JOY", "NEUTRAL", "SURPRISE"]
    assert results[1].dominant_emotion in ["ANGER", "DISGUST", "FEAR", "NEUTRAL"]

