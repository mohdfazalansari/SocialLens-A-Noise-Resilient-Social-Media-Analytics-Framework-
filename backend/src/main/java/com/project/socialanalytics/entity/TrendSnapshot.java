package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trend_snapshots")
public class TrendSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "raw_post_count")
    private Integer rawPostCount = 0;

    @Column(name = "unique_post_count")
    private Integer uniquePostCount = 0;

    @Column(name = "duplicate_ratio", precision = 5, scale = 4)
    private BigDecimal duplicateRatio = BigDecimal.ZERO;

    @Column(name = "growth_rate", precision = 7, scale = 4)
    private BigDecimal growthRate = BigDecimal.ZERO;

    @Column(name = "z_score", precision = 7, scale = 4)
    private BigDecimal zScore = BigDecimal.ZERO;

    @Column(name = "sentiment_shift", precision = 7, scale = 4)
    private BigDecimal sentimentShift = BigDecimal.ZERO;

    @Column(name = "trend_score", precision = 7, scale = 4)
    private BigDecimal trendScore = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String classification = "STABLE"; // EMERGING, RISING, STABLE, DECLINING

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public TrendSnapshot() {}

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

