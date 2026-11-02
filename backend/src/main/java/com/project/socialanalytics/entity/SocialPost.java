package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_posts")
public class SocialPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "platform_post_id", length = 100)
    private String platformPostId;

    @Column(length = 50)
    private String platform = "TWITTER";

    @Column(name = "author_id", length = 100)
    private String authorId;

    @Column(name = "post_text", nullable = false, columnDefinition = "TEXT")
    private String postText;

    @Column(name = "cleaned_text", columnDefinition = "TEXT")
    private String cleanedText;

    @Column(length = 20)
    private String language = "en";

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @Column(name = "share_count")
    private Integer shareCount = 0;

    @Column(name = "reply_to_post_id", length = 100)
    private String replyToPostId;

    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_group_id")
    private DuplicateGroup duplicateGroup;

    @Column(name = "post_created_at")
    private LocalDateTime postCreatedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (replyCount == null) replyCount = 0;
        if (shareCount == null) shareCount = 0;
        if (isDuplicate == null) isDuplicate = false;
    }

    public SocialPost() {}

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

    public DuplicateGroup getDuplicateGroup() {
        return duplicateGroup;
    }

    public void setDuplicateGroup(DuplicateGroup duplicateGroup) {
        this.duplicateGroup = duplicateGroup;
    }

    public LocalDateTime getPostCreatedAt() {
        return postCreatedAt;
    }

    public void setPostCreatedAt(LocalDateTime postCreatedAt) {
        this.postCreatedAt = postCreatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

