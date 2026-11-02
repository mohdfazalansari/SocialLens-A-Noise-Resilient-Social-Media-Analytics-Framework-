package com.project.socialanalytics.dto;

import java.util.Map;

public class AnalyticsOverviewDto {

    private Long totalPosts = 0L;
    private Long uniquePosts = 0L;
    private Long duplicatePosts = 0L;
    private Long positivePosts = 0L;
    private Long negativePosts = 0L;
    private Long neutralPosts = 0L;
    private Integer activeTopics = 0;
    private Integer emergingTrends = 0;
    private Double averageSentimentPolarity = 0.0;
    private Map<String, Long> platformBreakdown;
    private Map<String, Long> emotionBreakdown;

    public AnalyticsOverviewDto() {}

    public Long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Long getUniquePosts() {
        return uniquePosts;
    }

    public void setUniquePosts(Long uniquePosts) {
        this.uniquePosts = uniquePosts;
    }

    public Long getDuplicatePosts() {
        return duplicatePosts;
    }

    public void setDuplicatePosts(Long duplicatePosts) {
        this.duplicatePosts = duplicatePosts;
    }

    public Long getPositivePosts() {
        return positivePosts;
    }

    public void setPositivePosts(Long positivePosts) {
        this.positivePosts = positivePosts;
    }

    public Long getNegativePosts() {
        return negativePosts;
    }

    public void setNegativePosts(Long negativePosts) {
        this.negativePosts = negativePosts;
    }

    public Long getNeutralPosts() {
        return neutralPosts;
    }

    public void setNeutralPosts(Long neutralPosts) {
        this.neutralPosts = neutralPosts;
    }

    public Integer getActiveTopics() {
        return activeTopics;
    }

    public void setActiveTopics(Integer activeTopics) {
        this.activeTopics = activeTopics;
    }

    public Integer getEmergingTrends() {
        return emergingTrends;
    }

    public void setEmergingTrends(Integer emergingTrends) {
        this.emergingTrends = emergingTrends;
    }

    public Double getAverageSentimentPolarity() {
        return averageSentimentPolarity;
    }

    public void setAverageSentimentPolarity(Double averageSentimentPolarity) {
        this.averageSentimentPolarity = averageSentimentPolarity;
    }

    public Map<String, Long> getPlatformBreakdown() {
        return platformBreakdown;
    }

    public void setPlatformBreakdown(Map<String, Long> platformBreakdown) {
        this.platformBreakdown = platformBreakdown;
    }

    public Map<String, Long> getEmotionBreakdown() {
        return emotionBreakdown;
    }

    public void setEmotionBreakdown(Map<String, Long> emotionBreakdown) {
        this.emotionBreakdown = emotionBreakdown;
    }
}

