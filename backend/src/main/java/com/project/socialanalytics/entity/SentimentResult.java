package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_results")
public class SentimentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private SocialPost post;

    @Column(nullable = false, length = 30)
    private String label; // POSITIVE, NEGATIVE, NEUTRAL

    @Column(name = "positive_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal positiveScore;

    @Column(name = "negative_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal negativeScore;

    @Column(name = "neutral_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal neutralScore;

    @Column(name = "model_name", length = 150)
    private String modelName;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public SentimentResult() {}

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getPositiveScore() {
        return positiveScore;
    }

    public void setPositiveScore(BigDecimal positiveScore) {
        this.positiveScore = positiveScore;
    }

    public BigDecimal getNegativeScore() {
        return negativeScore;
    }

    public void setNegativeScore(BigDecimal negativeScore) {
        this.negativeScore = negativeScore;
    }

    public BigDecimal getNeutralScore() {
        return neutralScore;
    }

    public void setNeutralScore(BigDecimal neutralScore) {
        this.neutralScore = neutralScore;
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

