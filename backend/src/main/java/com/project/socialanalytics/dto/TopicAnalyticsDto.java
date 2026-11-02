package com.project.socialanalytics.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TopicAnalyticsDto {

    private Long id;
    private Integer topicIndex;
    private String name;
    private List<String> topKeywords;
    private Integer postCount;
    private BigDecimal representativeCoherence;
    private Map<String, Long> sentimentDistribution;

    public TopicAnalyticsDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTopicIndex() {
        return topicIndex;
    }

    public void setTopicIndex(Integer topicIndex) {
        this.topicIndex = topicIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTopKeywords() {
        return topKeywords;
    }

    public void setTopKeywords(List<String> topKeywords) {
        this.topKeywords = topKeywords;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public BigDecimal getRepresentativeCoherence() {
        return representativeCoherence;
    }

    public void setRepresentativeCoherence(BigDecimal representativeCoherence) {
        this.representativeCoherence = representativeCoherence;
    }

    public Map<String, Long> getSentimentDistribution() {
        return sentimentDistribution;
    }

    public void setSentimentDistribution(Map<String, Long> sentimentDistribution) {
        this.sentimentDistribution = sentimentDistribution;
    }
}

