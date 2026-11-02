import logging
from typing import List, Dict, Any
import numpy as np
import torch
from transformers import AutoModelForSequenceClassification, AutoTokenizer

from app.schemas.sentiment import TextItem, SentimentResult

logger = logging.getLogger("sentiment_service")

class SentimentClassifier:

    def __init__(self, model_name: str = "cardiffnlp/twitter-roberta-base-sentiment-latest"):
        self.model_name = model_name
        self.tokenizer = None
        self.model = None
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.labels = ["NEGATIVE", "NEUTRAL", "POSITIVE"]  # CardiffNLP standard mapping
        self.is_loaded = False

    def load_model(self):
        try:
            logger.info("Loading sentiment model: %s on device: %s", self.model_name, self.device)
            self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
            self.model = AutoModelForSequenceClassification.from_pretrained(self.model_name)
            self.model.to(self.device)
            self.model.eval()
            self.is_loaded = True
            logger.info("Sentiment model successfully loaded.")
        except Exception as e:
            logger.warning("Could not load HuggingFace sentiment model (%s): %s. Using lexical fallback.", self.model_name, e)
            self.is_loaded = False

    def predict_batch(self, items: List[TextItem]) -> List[SentimentResult]:
        if not items:
            return []

        results: List[SentimentResult] = []

        if self.is_loaded and self.tokenizer is not None and self.model is not None:
            try:
                texts = [item.text if item.text.strip() else "neutral" for item in items]
                encoded = self.tokenizer(
                    texts,
                    padding=True,
                    truncation=True,
                    max_length=128,
                    return_tensors="pt"
                ).to(self.device)

                with torch.no_grad():
                    outputs = self.model(**encoded)
                    scores = torch.softmax(outputs.logits, dim=-1).cpu().numpy()

                for item, score_vec in zip(items, scores):
                    neg, neu, pos = float(score_vec[0]), float(score_vec[1]), float(score_vec[2])
                    max_idx = int(np.argmax(score_vec))
                    pred_label = self.labels[max_idx]

                    results.append(SentimentResult(
                        id=item.id,
                        label=pred_label,
                        positive_score=round(pos, 4),
                        negative_score=round(neg, 4),
                        neutral_score=round(neu, 4),
                        model_name=self.model_name
                    ))
                return results
            except Exception as e:
                logger.error("HuggingFace batch sentiment inference failed: %s. Reverting to rule-based fallback.", e)

        # High-precision Lexicon Rule-Based Fallback
        for item in items:
            res = self._fallback_sentiment(item.id, item.text)
            results.append(res)

        return results

    def _fallback_sentiment(self, text_id: int, text: str) -> SentimentResult:
        t = text.lower()
        pos_words = {"good", "great", "excellent", "awesome", "love", "amazing", "best", "fantastic", "happy", "bullish", "profit", "gain", "solved", "fixed", "nice", "success", "helpful"}
        neg_words = {"bad", "terrible", "worst", "hate", "awful", "poor", "crash", "drain", "broken", "fraud", "scam", "loss", "fail", "slow", "bug", "error", "horrible", "dump"}

        tokens = set(t.split())
        p_count = sum(1 for w in tokens if w in pos_words)
        n_count = sum(1 for w in tokens if w in neg_words)

        if p_count > n_count:
            label = "POSITIVE"
            pos_score, neg_score, neu_score = 0.75, 0.10, 0.15
        elif n_count > p_count:
            label = "NEGATIVE"
            pos_score, neg_score, neu_score = 0.10, 0.75, 0.15
        else:
            label = "NEUTRAL"
            pos_score, neg_score, neu_score = 0.20, 0.20, 0.60

        return SentimentResult(
            id=text_id,
            label=label,
            positive_score=pos_score,
            negative_score=neg_score,
            neutral_score=neu_score,
            model_name="lexical-rule-engine-fallback"
        )

sentiment_classifier = SentimentClassifier()

