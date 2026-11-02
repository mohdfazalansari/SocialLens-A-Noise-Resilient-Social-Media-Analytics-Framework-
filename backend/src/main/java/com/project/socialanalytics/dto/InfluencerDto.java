package com.project.socialanalytics.dto;

import java.math.BigDecimal;

public class InfluencerDto {

    private String nodeId;
    private String pseudonym;
    private Integer inDegree;
    private Integer outDegree;
    private BigDecimal pagerank;
    private BigDecimal betweenness;
    private Integer communityId;

    public InfluencerDto() {}

    public InfluencerDto(String nodeId, String pseudonym, Integer inDegree, Integer outDegree, BigDecimal pagerank, BigDecimal betweenness, Integer communityId) {
        this.nodeId = nodeId;
        this.pseudonym = pseudonym;
        this.inDegree = inDegree;
        this.outDegree = outDegree;
        this.pagerank = pagerank;
        this.betweenness = betweenness;
        this.communityId = communityId;
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

    public BigDecimal getPagerank() {
        return pagerank;
    }

    public void setPagerank(BigDecimal pagerank) {
        this.pagerank = pagerank;
    }

    public BigDecimal getBetweenness() {
        return betweenness;
    }

    public void setBetweenness(BigDecimal betweenness) {
        this.betweenness = betweenness;
    }

    public Integer getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Integer communityId) {
        this.communityId = communityId;
    }
}

