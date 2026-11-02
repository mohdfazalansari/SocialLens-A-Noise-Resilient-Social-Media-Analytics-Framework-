package com.project.socialanalytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TrendAnalyticsDto {

    private String method; // BASELINE_VOLUME, BASELINE_GROWTH, PROPOSED_NOISE_RESILIENT
    private List<TrendSnapshotDto> snapshots;
    private List<TrendAlertDto> alerts;

    public static class TrendSnapshotDto {
        private Long id;
        private Long topicId;
        private String topicName;
        private LocalDateTime windowStart;
        private LocalDateTime windowEnd;
        private Integer rawPostCount;
        private Integer uniquePostCount;
        private BigDecimal duplicateRatio;
        private BigDecimal growthRate;
        private BigDecimal zScore;
        private BigDecimal sentimentShift;
        private BigDecimal trendScore;
        private String classification;

        public TrendSnapshotDto() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(LocalDateTime windowStart) {
            this.windowStart = windowStart;
        }

        public LocalDateTime getWindowEnd() {
            return windowEnd;
        }

        public void setWindowEnd(LocalDateTime windowEnd) {
            this.windowEnd = windowEnd;
        }

        public Integer getRawPostCount() {
            return rawPostCount;
        }

        public void setRawPostCount(Integer rawPostCount) {
            this.rawPostCount = rawPostCount;
        }

        public Integer getUniquePostCount() {
            return uniquePostCount;
        }

        public void setUniquePostCount(Integer uniquePostCount) {
            this.uniquePostCount = uniquePostCount;
        }

        public BigDecimal getDuplicateRatio() {
            return duplicateRatio;
        }

        public void setDuplicateRatio(BigDecimal duplicateRatio) {
            this.duplicateRatio = duplicateRatio;
        }

        public BigDecimal getGrowthRate() {
            return growthRate;
        }

        public void setGrowthRate(BigDecimal growthRate) {
            this.growthRate = growthRate;
        }

        public BigDecimal getZScore() {
            return zScore;
        }

        public void setZScore(BigDecimal zScore) {
            this.zScore = zScore;
        }

        public BigDecimal getSentimentShift() {
            return sentimentShift;
        }

        public void setSentimentShift(BigDecimal sentimentShift) {
            this.sentimentShift = sentimentShift;
        }

        public BigDecimal getTrendScore() {
            return trendScore;
        }

        public void setTrendScore(BigDecimal trendScore) {
            this.trendScore = trendScore;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }
    }

    public TrendAnalyticsDto() {}

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<TrendSnapshotDto> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<TrendSnapshotDto> snapshots) {
        this.snapshots = snapshots;
    }

    public List<TrendAlertDto> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<TrendAlertDto> alerts) {
        this.alerts = alerts;
    }
}

