package com.project.socialanalytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "evaluation_results")
public class EvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private EvaluationRun evaluationRun;

    @Column(name = "method_name", nullable = false, length = 50)
    private String methodName; // BASELINE_VOLUME, BASELINE_GROWTH, PROPOSED_NOISE_RESILIENT

    @Column(name = "precision_score", precision = 5, scale = 4)
    private BigDecimal precisionScore = BigDecimal.ZERO;

    @Column(name = "recall_score", precision = 5, scale = 4)
    private BigDecimal recallScore = BigDecimal.ZERO;

    @Column(name = "f1_score", precision = 5, scale = 4)
    private BigDecimal f1Score = BigDecimal.ZERO;

    @Column(name = "false_alert_rate", precision = 5, scale = 4)
    private BigDecimal falseAlertRate = BigDecimal.ZERO;

    @Column(name = "avg_detection_delay_hours", precision = 7, scale = 2)
    private BigDecimal avgDetectionDelayHours = BigDecimal.ZERO;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    public EvaluationResult() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvaluationRun getEvaluationRun() {
        return evaluationRun;
    }

    public void setEvaluationRun(EvaluationRun evaluationRun) {
        this.evaluationRun = evaluationRun;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public BigDecimal getPrecisionScore() {
        return precisionScore;
    }

    public void setPrecisionScore(BigDecimal precisionScore) {
        this.precisionScore = precisionScore;
    }

    public BigDecimal getRecallScore() {
        return recallScore;
    }

    public void setRecallScore(BigDecimal recallScore) {
        this.recallScore = recallScore;
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

    public String getMetricsJson() {
        return metricsJson;
    }

    public void setMetricsJson(String metricsJson) {
        this.metricsJson = metricsJson;
    }
}

