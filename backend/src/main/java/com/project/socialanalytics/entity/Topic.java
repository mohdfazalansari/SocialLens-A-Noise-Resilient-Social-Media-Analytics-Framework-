package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "topic_index", nullable = false)
    private Integer topicIndex;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "top_keywords", columnDefinition = "TEXT")
    private String topKeywords;

    @Column(name = "post_count")
    private Integer postCount = 0;

    @Column(name = "representative_coherence", precision = 5, scale = 4)
    private BigDecimal representativeCoherence = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (postCount == null) postCount = 0;
        if (representativeCoherence == null) representativeCoherence = BigDecimal.ZERO;
    }

    public Topic() {}

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

    public String getTopKeywords() {
        return topKeywords;
    }

    public void setTopKeywords(String topKeywords) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

