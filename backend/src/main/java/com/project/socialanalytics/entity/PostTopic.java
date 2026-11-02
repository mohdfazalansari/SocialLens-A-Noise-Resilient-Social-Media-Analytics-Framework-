package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "post_topics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "topic_id"})
})
public class PostTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private SocialPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(precision = 5, scale = 4)
    private BigDecimal probability = BigDecimal.valueOf(1.0000);

    public PostTopic() {}

    public PostTopic(SocialPost post, Topic topic, BigDecimal probability) {
        this.post = post;
        this.topic = topic;
        this.probability = probability != null ? probability : BigDecimal.valueOf(1.0000);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SocialPost getPost() {
        return post;
    }

    public void setPost(SocialPost post) {
        this.post = post;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public BigDecimal getProbability() {
        return probability;
    }

    public void setProbability(BigDecimal probability) {
        this.probability = probability;
    }
}

