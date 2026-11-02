package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "network_edges")
public class NetworkEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "source_node", nullable = false, length = 100)
    private String sourceNode;

    @Column(name = "target_node", nullable = false, length = 100)
    private String targetNode;

    @Column(name = "interaction_type", nullable = false, length = 30)
    private String interactionType = "REPLY"; // REPLY, MENTION, RETWEET

    @Column(nullable = false)
    private Integer weight = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public NetworkEdge() {}

    public NetworkEdge(Dataset dataset, String sourceNode, String targetNode, String interactionType, Integer weight) {
        this.dataset = dataset;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.interactionType = interactionType != null ? interactionType : "REPLY";
        this.weight = weight != null ? weight : 1;
    }

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

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

