package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trend_alerts")
public class TrendAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "alert_level", nullable = false, length = 30)
    private String alertLevel = "MEDIUM"; // HIGH, MEDIUM, LOW

    @Column(nullable = false, length = 30)
    private String classification = "EMERGING";

    @Column(name = "trend_score", nullable = false, precision = 7, scale = 4)
    private BigDecimal trendScore;

    @Column(name = "baseline_volume", precision = 7, scale = 2)
    private BigDecimal baselineVolume = BigDecimal.ZERO;

    @Column(name = "current_volume", precision = 7, scale = 2)
    private BigDecimal currentVolume = BigDecimal.ZERO;

    @Column(name = "growth_percentage", precision = 7, scale = 2)
    private BigDecimal growthPercentage = BigDecimal.ZERO;

    @Column(name = "sentiment_shift", precision = 7, scale = 4)
    private BigDecimal sentimentShift = BigDecimal.ZERO;

    @Column(name = "duplicate_ratio", precision = 5, scale = 4)
    private BigDecimal duplicateRatio = BigDecimal.ZERO;

    @Column(name = "alert_reason", columnDefinition = "TEXT")
    private String alertReason;

    @Column(name = "supporting_post_ids", columnDefinition = "JSON")
    private String supportingPostIds;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) detectedAt = LocalDateTime.now();
    }

    public TrendAlert() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public BigDecimal getTrendScore() {
        return trendScore;
    }

    public void setTrendScore(BigDecimal trendScore) {
        this.trendScore = trendScore;
    }

    public BigDecimal getBaselineVolume() {
        return baselineVolume;
    }

    public void setBaselineVolume(BigDecimal baselineVolume) {
        this.baselineVolume = baselineVolume;
    }

    public BigDecimal getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(BigDecimal currentVolume) {
        this.currentVolume = currentVolume;
    }

    public BigDecimal getGrowthPercentage() {
        return growthPercentage;
    }

    public void setGrowthPercentage(BigDecimal growthPercentage) {
        this.growthPercentage = growthPercentage;
    }

    public BigDecimal getSentimentShift() {
        return sentimentShift;
    }

    public void setSentimentShift(BigDecimal sentimentShift) {
        this.sentimentShift = sentimentShift;
    }

    public BigDecimal getDuplicateRatio() {
        return duplicateRatio;
    }

    public void setDuplicateRatio(BigDecimal duplicateRatio) {
        this.duplicateRatio = duplicateRatio;
    }

    public String getAlertReason() {
        return alertReason;
    }

    public void setAlertReason(String alertReason) {
        this.alertReason = alertReason;
    }

    public String getSupportingPostIds() {
        return supportingPostIds;
    }

    public void setSupportingPostIds(String supportingPostIds) {
        this.supportingPostIds = supportingPostIds;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}

