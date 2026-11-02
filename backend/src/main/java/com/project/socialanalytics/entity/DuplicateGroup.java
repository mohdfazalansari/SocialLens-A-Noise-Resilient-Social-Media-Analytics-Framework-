package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "duplicate_groups")
public class DuplicateGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_hash", nullable = false, unique = true, length = 64)
    private String groupHash;

    @Column(name = "duplicate_type", nullable = false, length = 30)
    private String duplicateType = "EXACT"; // EXACT, NEAR_DUPLICATE

    @Column(name = "similarity_score", precision = 5, scale = 4)
    private BigDecimal similarityScore = BigDecimal.valueOf(1.0000);

    @Column(name = "canonical_post_id")
    private Long canonicalPostId;

    @Column(name = "member_count")
    private Integer memberCount = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public DuplicateGroup() {}

    public DuplicateGroup(String groupHash, String duplicateType, BigDecimal similarityScore, Long canonicalPostId, Integer memberCount) {
        this.groupHash = groupHash;
        this.duplicateType = duplicateType;
        this.similarityScore = similarityScore;
        this.canonicalPostId = canonicalPostId;
        this.memberCount = memberCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupHash() {
        return groupHash;
    }

    public void setGroupHash(String groupHash) {
        this.groupHash = groupHash;
    }

    public String getDuplicateType() {
        return duplicateType;
    }

    public void setDuplicateType(String duplicateType) {
        this.duplicateType = duplicateType;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Long getCanonicalPostId() {
        return canonicalPostId;
    }

    public void setCanonicalPostId(Long canonicalPostId) {
        this.canonicalPostId = canonicalPostId;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

