package com.project.socialanalytics.dto;

import java.util.Map;

public class EmotionAnalyticsDto {

    private Map<String, Long> distribution;
    private String primaryEmotion;
    private Double primaryEmotionPercentage;

    public EmotionAnalyticsDto() {}

    public Map<String, Long> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<String, Long> distribution) {
        this.distribution = distribution;
    }

    public String getPrimaryEmotion() {
        return primaryEmotion;
    }

    public void setPrimaryEmotion(String primaryEmotion) {
        this.primaryEmotion = primaryEmotion;
    }

    public Double getPrimaryEmotionPercentage() {
        return primaryEmotionPercentage;
    }

    public void setPrimaryEmotionPercentage(Double primaryEmotionPercentage) {
        this.primaryEmotionPercentage = primaryEmotionPercentage;
    }
}

