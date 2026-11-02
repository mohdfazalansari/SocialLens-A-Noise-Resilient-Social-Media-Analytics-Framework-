package com.project.socialanalytics.dto;

import java.math.BigDecimal;

public class AnalysisConfigDto {

    private Double duplicateSimilarityThreshold = 0.85;
    private Integer minTopicSize = 3;
    private BigDecimal trendAlpha = BigDecimal.valueOf(0.40); // Z-Score weight
    private BigDecimal trendBeta = BigDecimal.valueOf(0.30);  // Growth rate weight
    private BigDecimal trendGamma = BigDecimal.valueOf(0.20); // Sentiment shift weight
    private BigDecimal trendDelta = BigDecimal.valueOf(0.10); // Engagement weight
    private Integer windowSizeHours = 24;

    public AnalysisConfigDto() {}

    public Double getDuplicateSimilarityThreshold() {
        return duplicateSimilarityThreshold;
    }

    public void setDuplicateSimilarityThreshold(Double duplicateSimilarityThreshold) {
        this.duplicateSimilarityThreshold = duplicateSimilarityThreshold;
    }

    public Integer getMinTopicSize() {
        return minTopicSize;
    }

    public void setMinTopicSize(Integer minTopicSize) {
        this.minTopicSize = minTopicSize;
    }

    public BigDecimal getTrendAlpha() {
        return trendAlpha;
    }

    public void setTrendAlpha(BigDecimal trendAlpha) {
        this.trendAlpha = trendAlpha;
    }

    public BigDecimal getTrendBeta() {
        return trendBeta;
    }

    public void setTrendBeta(BigDecimal trendBeta) {
        this.trendBeta = trendBeta;
    }

    public BigDecimal getTrendGamma() {
        return trendGamma;
    }

    public void setTrendGamma(BigDecimal trendGamma) {
        this.trendGamma = trendGamma;
    }

    public BigDecimal getTrendDelta() {
        return trendDelta;
    }

    public void setTrendDelta(BigDecimal trendDelta) {
        this.trendDelta = trendDelta;
    }

    public Integer getWindowSizeHours() {
        return windowSizeHours;
    }

    public void setWindowSizeHours(Integer windowSizeHours) {
        this.windowSizeHours = windowSizeHours;
    }
}

