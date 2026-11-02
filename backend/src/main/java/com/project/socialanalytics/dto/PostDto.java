package com.project.socialanalytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    private Long id;
    private Long datasetId;
    private String platformPostId;
    private String platform;
    private String authorId;
    private String postText;
    private String cleanedText;
    private String language;
    private Integer likeCount;
    private Integer replyCount;
    private Integer shareCount;
    private String replyToPostId;
    private Boolean isDuplicate;
    private Long duplicateGroupId;
    private String sentimentLabel;
    private BigDecimal sentimentConfidence;
    private String dominantEmotion;
    private BigDecimal emotionConfidence;
    private List<String> topics;
    private LocalDateTime postCreatedAt;

    public PostDto() {}

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

    public String getPlatformPostId() {
        return platformPostId;
    }

    public void setPlatformPostId(String platformPostId) {
        this.platformPostId = platformPostId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getCleanedText() {
        return cleanedText;
    }

    public void setCleanedText(String cleanedText) {
        this.cleanedText = cleanedText;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public String getReplyToPostId() {
        return replyToPostId;
    }

    public void setReplyToPostId(String replyToPostId) {
        this.replyToPostId = replyToPostId;
    }

    public Boolean getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(Boolean duplicate) {
        isDuplicate = duplicate;
    }

    public Long getDuplicateGroupId() {
        return duplicateGroupId;
    }

    public void setDuplicateGroupId(Long duplicateGroupId) {
        this.duplicateGroupId = duplicateGroupId;
    }

    public String getSentimentLabel() {
        return sentimentLabel;
    }

    public void setSentimentLabel(String sentimentLabel) {
        this.sentimentLabel = sentimentLabel;
    }

    public BigDecimal getSentimentConfidence() {
        return sentimentConfidence;
    }

    public void setSentimentConfidence(BigDecimal sentimentConfidence) {
        this.sentimentConfidence = sentimentConfidence;
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

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public LocalDateTime getPostCreatedAt() {
        return postCreatedAt;
    }

    public void setPostCreatedAt(LocalDateTime postCreatedAt) {
        this.postCreatedAt = postCreatedAt;
    }
}

