package com.project.socialanalytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class NetworkGraphDto {

    private List<NodeDto> nodes;
    private List<EdgeDto> edges;
    private NetworkMetricsDto metrics;

    public static class NodeDto {
        private String id;
        private String label;
        private Integer degree;
        private BigDecimal pagerank;
        private BigDecimal betweenness;
        private Integer community;

        public NodeDto() {}

        public NodeDto(String id, String label, Integer degree, BigDecimal pagerank, BigDecimal betweenness, Integer community) {
            this.id = id;
            this.label = label;
            this.degree = degree;
            this.pagerank = pagerank;
            this.betweenness = betweenness;
            this.community = community;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Integer getDegree() {
            return degree;
        }

        public void setDegree(Integer degree) {
            this.degree = degree;
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

        public Integer getCommunity() {
            return community;
        }

        public void setCommunity(Integer community) {
            this.community = community;
        }
    }

    public static class EdgeDto {
        private String source;
        private String target;
        private String type;
        private Integer weight;

        public EdgeDto() {}

        public EdgeDto(String source, String target, String type, Integer weight) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.weight = weight;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }
    }

    public static class NetworkMetricsDto {
        private int totalNodes;
        private int totalEdges;
        private double density;
        private int connectedComponents;

        public NetworkMetricsDto() {}

        public NetworkMetricsDto(int totalNodes, int totalEdges, double density, int connectedComponents) {
            this.totalNodes = totalNodes;
            this.totalEdges = totalEdges;
            this.density = density;
            this.connectedComponents = connectedComponents;
        }

        public int getTotalNodes() {
            return totalNodes;
        }

        public void setTotalNodes(int totalNodes) {
            this.totalNodes = totalNodes;
        }

        public int getTotalEdges() {
            return totalEdges;
        }

        public void setTotalEdges(int totalEdges) {
            this.totalEdges = totalEdges;
        }

        public double getDensity() {
            return density;
        }

        public void setDensity(double density) {
            this.density = density;
        }

        public int getConnectedComponents() {
            return connectedComponents;
        }

        public void setConnectedComponents(int connectedComponents) {
            this.connectedComponents = connectedComponents;
        }
    }

    public NetworkGraphDto() {}

    public List<NodeDto> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDto> nodes) {
        this.nodes = nodes;
    }

    public List<EdgeDto> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeDto> edges) {
        this.edges = edges;
    }

    public NetworkMetricsDto getMetrics() {
        return metrics;
    }

    public void setMetrics(NetworkMetricsDto metrics) {
        this.metrics = metrics;
    }
}

