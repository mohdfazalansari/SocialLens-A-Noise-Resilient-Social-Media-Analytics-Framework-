package com.project.socialanalytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EvaluationReportDto {

    private Long runId;
    private Long datasetId;
    private String runName;
    private String groundTruthSource;
    private LocalDateTime executedAt;
    private List<MethodResultDto> methodResults;

    public static class MethodResultDto {
        private String methodName;
        private BigDecimal precision;
        private BigDecimal recall;
        private BigDecimal f1Score;
        private BigDecimal falseAlertRate;
        private BigDecimal avgDetectionDelayHours;
        private String detailedMetricsJson;

        public MethodResultDto() {}

        public MethodResultDto(String methodName, BigDecimal precision, BigDecimal recall, BigDecimal f1Score, BigDecimal falseAlertRate, BigDecimal avgDetectionDelayHours, String detailedMetricsJson) {
            this.methodName = methodName;
            this.precision = precision;
            this.recall = recall;
            this.f1Score = f1Score;
            this.falseAlertRate = falseAlertRate;
            this.avgDetectionDelayHours = avgDetectionDelayHours;
            this.detailedMetricsJson = detailedMetricsJson;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public BigDecimal getPrecision() {
            return precision;
        }

        public void setPrecision(BigDecimal precision) {
            this.precision = precision;
        }

        public BigDecimal getRecall() {
            return recall;
        }

        public void setRecall(BigDecimal recall) {
            this.recall = recall;
        }

        public BigDecimal getF1Score() {
            return f1Score;
        }

        public void setF1Score(BigDecimal f1Score) {
            this.f1Score = f1Score;
        }

        public BigDecimal getFalseAlertRate() {
            return falseAlertRate;
        }

        public void setFalseAlertRate(BigDecimal falseAlertRate) {
            this.falseAlertRate = falseAlertRate;
        }

        public BigDecimal getAvgDetectionDelayHours() {
            return avgDetectionDelayHours;
        }

        public void setAvgDetectionDelayHours(BigDecimal avgDetectionDelayHours) {
            this.avgDetectionDelayHours = avgDetectionDelayHours;
        }

        public String getDetailedMetricsJson() {
            return detailedMetricsJson;
        }

        public void setDetailedMetricsJson(String detailedMetricsJson) {
            this.detailedMetricsJson = detailedMetricsJson;
        }
    }

    public EvaluationReportDto() {}

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getGroundTruthSource() {
        return groundTruthSource;
    }

    public void setGroundTruthSource(String groundTruthSource) {
        this.groundTruthSource = groundTruthSource;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public List<MethodResultDto> getMethodResults() {
        return methodResults;
    }

    public void setMethodResults(List<MethodResultDto> methodResults) {
        this.methodResults = methodResults;
    }
}

