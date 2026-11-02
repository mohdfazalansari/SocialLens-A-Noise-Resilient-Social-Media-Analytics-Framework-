# System Architecture & Technical Specifications

## 1. Architectural Overview

**SocialLens** employs a decoupled three-tier microservice architecture designed for maintainability, security, and academic evaluation.

```mermaid
flowchart TD
    subgraph Client ["Client Presentation Layer (React + Vite + Tailwind)"]
        UI["React SPA (Dashboard, Datasets, Posts, Sentiment, Topics, Trends, Network, Evaluation)"]
        State["Axios REST Client + JWT Session Context"]
    end

    subgraph Backend ["Business & Orchestration Layer (Java 17+ Spring Boot)"]
        Sec["Spring Security & JJWT Filter"]
        Ctrl["REST Controller Layer (/api/*)"]
        Svc["Service Layer & Asynchronous Job Orchestrator"]
        AIClient["FastAPI HTTP Client"]
        Flyway["Flyway Database Migration Engine"]
    end

    subgraph Storage ["Relational Persistence (MySQL 8.0)"]
        DB[(MySQL Database: social_analytics_db)]
    end

    subgraph AIService ["Inference & Analytical Engine (Python FastAPI)"]
        FastAPIApp["FastAPI REST App (/ai/*)"]
        Preproc["Text Normalization & Regex Cleaner"]
        Duplicates["MD5 Exact Hashing + TF-IDF Cosine Clustering"]
        Sentiment["CardiffNLP Twitter-RoBERTa Sentiment"]
        Emotion["DistilRoBERTa 7-Class Emotion"]
        Topics["BERTopic + Sentence-Transformers (all-MiniLM-L6-v2)"]
        Trends["Noise-Resilient Trend Anomaly Engine"]
        Graph["NetworkX Graph Centrality & Community Detection"]
        Eval["Research Benchmark & Evaluation Runner"]
    end

    UI -->|"JSON / Bearer Token"| Ctrl
    Ctrl --> Sec
    Ctrl --> Svc
    Svc -->|"Spring Data JPA"| DB
    Flyway -->|"Schema Versioning"| DB
    Svc -->|"Async HTTP Calls"| AIClient
    AIClient -->|"Internal Microservice REST"| FastAPIApp
    FastAPIApp --> Preproc
    FastAPIApp --> Duplicates
    FastAPIApp --> Sentiment
    FastAPIApp --> Emotion
    FastAPIApp --> Topics
    FastAPIApp --> Trends
    FastAPIApp --> Graph
    FastAPIApp --> Eval
```

---

## 2. Component Boundaries & Responsibilities

### A. React Frontend (Port 5173)
- Built with **React 18**, **Vite**, **Tailwind CSS**, and **Lucide React**.
- Visualizations implemented via **Recharts** (Pie, Line, Bar charts) and dynamic SVG topology graph.
- Communicates **exclusively** with the Spring Boot backend (`http://localhost:8080/api`), maintaining architectural encapsulation.

### B. Spring Boot Main Backend (Port 8080)
- Built with **Java 17/23**, **Spring Boot 3.3.0**, **Spring Data JPA**, and **Spring Security**.
- Manages user authentication, BCrypt password hashing, and stateless JWT verification.
- Ingests tabular (CSV) and structured (JSON) datasets with schema mapping and preview.
- Orchestrates asynchronous analytics jobs via `AnalysisOrchestratorService`.
- Connects directly to **MySQL 8.0** with Flyway versioned database migrations.

### C. Python FastAPI AI Service (Port 8000)
- Built with **Python 3.13**, **FastAPI**, **Uvicorn**, **PyTorch**, **Transformers**, **Sentence-Transformers**, **BERTopic**, **scikit-learn**, and **NetworkX**.
- Operates as a stateless analytical engine with single-instance model caching during startup.
- Implements resilient fallbacks for zero-failure runtime guarantees.

---

## 3. Future Social Media API Connectors Interface

To comply with platform terms of service and avoid unauthorized scraping, the system defines clean abstract connectors for future authorized API additions:

```java
public interface SocialMediaConnector {
    String getPlatformName();
    boolean validateCredentials(Map<String, String> credentials);
    List<SocialPost> fetchAuthorizedPublicPosts(String query, int maxResults);
}
```

Authorized credentials (e.g., X API Bearer Token, Telegram Bot Token) are configured strictly via environment variables (`X_API_BEARER_TOKEN`, `TELEGRAM_BOT_TOKEN`), never hardcoded into source code.

