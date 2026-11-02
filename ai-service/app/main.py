import os
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import torch

from app.api.routes import router as ai_router
from app.services.sentiment_service import sentiment_classifier
from app.services.emotion_service import emotion_classifier
from app.services.topic_service import topic_discoverer

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("sociallens_ai")

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Initializing SocialLens AI Microservice...")
    # Attempt loading models if explicit preloading is enabled or on-demand
    # Note: fallback engines are ready immediately
    preload = os.getenv("PRELOAD_MODELS", "false").lower() == "true"
    if preload:
        sentiment_classifier.load_model()
        emotion_classifier.load_model()
        topic_discoverer.load_model()
    else:
        logger.info("Preloading disabled. Models will load on demand or use resilient lightweight analytical engines.")
    yield
    logger.info("SocialLens AI Microservice shutting down.")

app = FastAPI(
    title="SocialLens AI Microservice",
    description="Inference and analytical backend for noise-resilient trend detection, sentiment/emotion classification, topic discovery, and social network analysis.",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(ai_router, prefix="/ai", tags=["AI Analytics"])

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "SocialLens AI Engine",
        "cuda_available": torch.cuda.is_available(),
        "device": "cuda" if torch.cuda.is_available() else "cpu",
        "sentiment_loaded": sentiment_classifier.is_loaded,
        "emotion_loaded": emotion_classifier.is_loaded,
        "topic_loaded": topic_discoverer.is_loaded
    }

@app.get("/")
def root():
    return {
        "name": "SocialLens AI Microservice",
        "status": "operational",
        "docs_url": "/docs"
    }

