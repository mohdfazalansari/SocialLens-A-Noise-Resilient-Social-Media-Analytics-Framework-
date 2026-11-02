package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_results")
public class EmotionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private SocialPost post;

    @Column(name = "dominant_emotion", nullable = false, length = 30)
    private String dominantEmotion; // JOY, ANGER, SADNESS, FEAR, SURPRISE, DISGUST, NEUTRAL

    @Column(name = "emotion_confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal emotionConfidence;

    @Column(name = "emotion_scores", columnDefinition = "JSON")
    private String emotionScores;

    @Column(name = "model_name", length = 150)
    private String modelName;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public EmotionResult() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SocialPost getPost() {
        return post;
    }

    public void setPost(SocialPost post) {
        this.post = post;
    }

    public String getDominantEmotion() {
        return dominantEmotion;
    }

    public void setDominantEmotion(String dominantEmotion) {
        this.dominantEmotion = dominantEmotion;
    }

    public BigDecimal getEmotionConfidence() {
        return emotionConfidence;
    }

    public void setEmotionConfidence(BigDecimal emotionConfidence) {
        this.emotionConfidence = emotionConfidence;
    }

    public String getEmotionScores() {
        return emotionScores;
    }

    public void setEmotionScores(String emotionScores) {
        this.emotionScores = emotionScores;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

