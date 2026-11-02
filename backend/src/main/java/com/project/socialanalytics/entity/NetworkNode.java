package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "network_nodes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dataset_id", "node_id"})
})
public class NetworkNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "node_id", nullable = false, length = 100)
    private String nodeId;

    @Column(nullable = false, length = 100)
    private String pseudonym;

    @Column(name = "in_degree")
    private Integer inDegree = 0;

    @Column(name = "out_degree")
    private Integer outDegree = 0;

    @Column(name = "betweenness_centrality", precision = 7, scale = 6)
    private BigDecimal betweennessCentrality = BigDecimal.ZERO;

    @Column(precision = 7, scale = 6)
    private BigDecimal pagerank = BigDecimal.ZERO;

    @Column(name = "community_id")
    private Integer communityId = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public NetworkNode() {}

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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    public Integer getInDegree() {
        return inDegree;
    }

    public void setInDegree(Integer inDegree) {
        this.inDegree = inDegree;
    }

    public Integer getOutDegree() {
        return outDegree;
    }

    public void setOutDegree(Integer outDegree) {
        this.outDegree = outDegree;
    }

    public BigDecimal getBetweennessCentrality() {
        return betweennessCentrality;
    }

    public void setBetweennessCentrality(BigDecimal betweennessCentrality) {
        this.betweennessCentrality = betweennessCentrality;
    }

    public BigDecimal getPagerank() {
        return pagerank;
    }

    public void setPagerank(BigDecimal pagerank) {
        this.pagerank = pagerank;
    }

    public Integer getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Integer communityId) {
        this.communityId = communityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

