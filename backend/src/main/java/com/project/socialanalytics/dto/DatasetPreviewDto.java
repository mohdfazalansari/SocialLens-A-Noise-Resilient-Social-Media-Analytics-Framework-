package com.project.socialanalytics.dto;

import java.util.List;
import java.util.Map;

public class DatasetPreviewDto {

    private List<String> headers;
    private List<Map<String, String>> sampleRows;
    private int totalDetectedRows;
    private Map<String, String> suggestedColumnMappings;

    public DatasetPreviewDto() {}

    public DatasetPreviewDto(List<String> headers, List<Map<String, String>> sampleRows, int totalDetectedRows, Map<String, String> suggestedColumnMappings) {
        this.headers = headers;
        this.sampleRows = sampleRows;
        this.totalDetectedRows = totalDetectedRows;
        this.suggestedColumnMappings = suggestedColumnMappings;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Map<String, String>> getSampleRows() {
        return sampleRows;
    }

    public void setSampleRows(List<Map<String, String>> sampleRows) {
        this.sampleRows = sampleRows;
    }

    public int getTotalDetectedRows() {
        return totalDetectedRows;
    }

    public void setTotalDetectedRows(int totalDetectedRows) {
        this.totalDetectedRows = totalDetectedRows;
    }

    public Map<String, String> getSuggestedColumnMappings() {
        return suggestedColumnMappings;
    }

    public void setSuggestedColumnMappings(Map<String, String> suggestedColumnMappings) {
        this.suggestedColumnMappings = suggestedColumnMappings;
    }
}

