package com.project.socialanalytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TrendAlertDto {

    private Long id;
    private Long datasetId;
    private Long topicId;
    private String topicName;
    private String alertLevel;
    private String classification;
    private BigDecimal trendScore;
    private BigDecimal baselineVolume;
    private BigDecimal currentVolume;
    private BigDecimal growthPercentage;
    private BigDecimal sentimentShift;
    private BigDecimal duplicateRatio;
    private String alertReason;
    private List<Long> supportingPostIds;
    private LocalDateTime detectedAt;

    public TrendAlertDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
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

    public List<Long> getSupportingPostIds() {
        return supportingPostIds;
    }

    public void setSupportingPostIds(List<Long> supportingPostIds) {
        this.supportingPostIds = supportingPostIds;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}

