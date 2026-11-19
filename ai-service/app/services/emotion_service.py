import logging
import re
from typing import List, Dict, Any
import numpy as np
import torch
from transformers import AutoModelForSequenceClassification, AutoTokenizer

from app.schemas.sentiment import TextItem
from app.schemas.emotion import EmotionResult

logger = logging.getLogger("emotion_service")

class EmotionClassifier:

    def __init__(self, model_name: str = "j-hartmann/emotion-english-distilroberta-base"):
        self.model_name = model_name
        self.tokenizer = None
        self.model = None
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.labels = ["anger", "disgust", "fear", "joy", "neutral", "sadness", "surprise"]
        self.is_loaded = False
        self._attempted_load = False

    def load_model(self):
        try:
            logger.info("Loading emotion model: %s on device: %s", self.model_name, self.device)
            self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
            self.model = AutoModelForSequenceClassification.from_pretrained(self.model_name)
            self.model.to(self.device)
            self.model.eval()
            self.is_loaded = True
            logger.info("Emotion model successfully loaded.")
        except Exception as e:
            logger.warning("Could not load HuggingFace emotion model (%s): %s. Using lexical fallback.", self.model_name, e)
            self.is_loaded = False

    def predict_batch(self, items: List[TextItem]) -> List[EmotionResult]:
        if not items:
            return []

        if not self.is_loaded and not self._attempted_load:
            self._attempted_load = True
            self.load_model()

        results: List[EmotionResult] = []

        if self.is_loaded and self.tokenizer is not None and self.model is not None:
            try:
                texts = [item.text if item.text.strip() else "neutral statement" for item in items]
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
                    score_dict = {label: round(float(score_vec[i]), 4) for i, label in enumerate(self.labels)}
                    dominant_idx = int(np.argmax(score_vec))
                    dominant_emotion = self.labels[dominant_idx].upper()
                    conf = float(score_vec[dominant_idx])

                    results.append(EmotionResult(
                        id=item.id,
                        dominant_emotion=dominant_emotion,
                        confidence=round(conf, 4),
                        scores=score_dict,
                        model_name=self.model_name
                    ))
                return results
            except Exception as e:
                logger.error("HuggingFace emotion inference failed: %s. Using fallback.", e)

        # Lexicon Fallback
        for item in items:
            results.append(self._fallback_emotion(item.id, item.text))

        return results

    def _fallback_emotion(self, text_id: int, text: str) -> EmotionResult:
        t = text.lower()
        mapping = {
            "joy": ["happy", "love", "lov", "delight", "great", "awesom", "excit", "win", "glad", "smooth", "impress", "breathtak", "stunn", "crisp", "super", "favorit", "clarit", "gorgeou", "proud", "like", "obsess"],
            "anger": ["furious", "angry", "outrage", "hate", "mad", "annoy", "piss", "scam", "terribl", "horribl", "worst", "unaccept", "rip", "broken", "brok", "fraud", "dump"],
            "sadness": ["sad", "depress", "unhapp", "cry", "tragic", "grief", "sorr", "drain", "drainage", "drains", "draining", "disappoint", "loses", "losing", "slow", "fail"],
            "fear": ["scared", "afraid", "panic", "fear", "danger", "risky", "worried", "warn", "warning", "threat", "freez", "froze", "frozen", "brick", "crash", "crashed", "crashing", "crashes"],
            "surprise": ["wow", "shock", "unexpected", "surprise", "unbeliev", "omg", "whoa", "breathtak"],
            "disgust": ["gross", "disgust", "nasty", "vile", "revolt", "garbage", "trash"],
        }

        detected = "NEUTRAL"
        max_c = 0
        tokens = set(re.findall(r'[a-z]+', t))

        for emo, words in mapping.items():
            cnt = sum(1 for w in tokens if any(w.startswith(stem) or stem in w for stem in words))
            if cnt > max_c:
                max_c = cnt
                detected = emo.upper()

        scores = {label: 0.05 for label in self.labels}
        scores[detected.lower()] = round(min(0.85, 0.50 + 0.10 * max_c), 4)
        rem = round(1.0 - scores[detected.lower()], 4)
        for label in self.labels:
            if label != detected.lower():
                scores[label] = round(rem / (len(self.labels) - 1), 4)

        conf = scores[detected.lower()]

        return EmotionResult(
            id=text_id,
            dominant_emotion=detected,
            confidence=conf,
            scores=scores,
            model_name="lexical-emotion-fallback"
        )

emotion_classifier = EmotionClassifier()

