package com.project.socialanalytics.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class DatasetDetailDto {

    private Long id;
    private String name;
    private String description;
    private String fileFormat;
    private Integer totalRecords;
    private Long uniqueRecords;
    private Long duplicateRecords;
    private String analysisStatus;
    private String uploadedByUsername;
    private Map<String, Long> platformDistribution;
    private Map<String, Long> languageDistribution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DatasetDetailDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Long getUniqueRecords() {
        return uniqueRecords;
    }

    public void setUniqueRecords(Long uniqueRecords) {
        this.uniqueRecords = uniqueRecords;
    }

    public Long getDuplicateRecords() {
        return duplicateRecords;
    }

    public void setDuplicateRecords(Long duplicateRecords) {
        this.duplicateRecords = duplicateRecords;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public String getUploadedByUsername() {
        return uploadedByUsername;
    }

    public void setUploadedByUsername(String uploadedByUsername) {
        this.uploadedByUsername = uploadedByUsername;
    }

    public Map<String, Long> getPlatformDistribution() {
        return platformDistribution;
    }

    public void setPlatformDistribution(Map<String, Long> platformDistribution) {
        this.platformDistribution = platformDistribution;
    }

    public Map<String, Long> getLanguageDistribution() {
        return languageDistribution;
    }

    public void setLanguageDistribution(Map<String, Long> languageDistribution) {
        this.languageDistribution = languageDistribution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

