package com.project.socialanalytics.dto;

import java.util.List;
import java.util.Map;

public class SentimentAnalyticsDto {

    private Map<String, Long> distribution;
    private List<TimePoint> overTime;
    private Double netSentimentScore; // (Pos - Neg) / Total

    public static class TimePoint {
        private String date;
        private Long positive;
        private Long negative;
        private Long neutral;

        public TimePoint() {}

        public TimePoint(String date, Long positive, Long negative, Long neutral) {
            this.date = date;
            this.positive = positive;
            this.negative = negative;
            this.neutral = neutral;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getPositive() {
            return positive;
        }

        public void setPositive(Long positive) {
            this.positive = positive;
        }

        public Long getNegative() {
            return negative;
        }

        public void setNegative(Long negative) {
            this.negative = negative;
        }

        public Long getNeutral() {
            return neutral;
        }

        public void setNeutral(Long neutral) {
            this.neutral = neutral;
        }
    }

    public SentimentAnalyticsDto() {}

    public Map<String, Long> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<String, Long> distribution) {
        this.distribution = distribution;
    }

    public List<TimePoint> getOverTime() {
        return overTime;
    }

    public void setOverTime(List<TimePoint> overTime) {
        this.overTime = overTime;
    }

    public Double getNetSentimentScore() {
        return netSentimentScore;
    }

    public void setNetSentimentScore(Double netSentimentScore) {
        this.netSentimentScore = netSentimentScore;
    }
}

