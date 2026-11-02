package com.project.socialanalytics.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.socialanalytics.dto.*;
import com.project.socialanalytics.entity.AnalyticsJob;
import com.project.socialanalytics.repository.AnalyticsJobRepository;
import com.project.socialanalytics.security.CurrentUser;
import com.project.socialanalytics.security.UserPrincipal;
import com.project.socialanalytics.service.AnalysisOrchestratorService;
import com.project.socialanalytics.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/datasets")
@Tag(name = "Datasets", description = "Dataset upload, preview, validation, and analytics execution")
public class DatasetController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private AnalysisOrchestratorService analysisOrchestratorService;

    @Autowired
    private AnalyticsJobRepository analyticsJobRepository;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Preview uploaded dataset and suggest column mappings")
    public ResponseEntity<ApiResponse<DatasetPreviewDto>> previewDataset(@RequestParam("file") MultipartFile file) {
        DatasetPreviewDto preview = datasetService.previewFile(file);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and import a social media dataset (CSV/JSON)")
    public ResponseEntity<ApiResponse<DatasetDetailDto>> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "columnMapping", required = false) String columnMappingJson,
            @CurrentUser UserPrincipal currentUser) {

        Map<String, String> mapping = Collections.emptyMap();
        if (columnMappingJson != null && !columnMappingJson.trim().isEmpty()) {
            try {
                mapping = objectMapper.readValue(columnMappingJson, new TypeReference<>() {});
            } catch (Exception ignored) {}
        }

        DatasetDetailDto dataset = datasetService.importDataset(file, name, description, mapping, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Dataset imported successfully", dataset));
    }

    @GetMapping
    @Operation(summary = "List all datasets with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<DatasetSummaryDto>>> getDatasets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PagedResponse<DatasetSummaryDto> datasets = datasetService.getAllDatasets(page, size);
        return ResponseEntity.ok(ApiResponse.success(datasets));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed statistics and summary for a dataset")
    public ResponseEntity<ApiResponse<DatasetDetailDto>> getDatasetById(@PathVariable("id") Long id) {
        DatasetDetailDto dataset = datasetService.getDatasetDetails(id);
        return ResponseEntity.ok(ApiResponse.success(dataset));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a dataset and all associated posts and analysis results")
    public ResponseEntity<ApiResponse<Void>> deleteDataset(@PathVariable("id") Long id) {
        datasetService.deleteDataset(id);
        return ResponseEntity.ok(ApiResponse.success("Dataset deleted successfully", null));
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger full AI analytics pipeline for a dataset")
    public ResponseEntity<ApiResponse<AnalyticsJob>> triggerAnalysis(
            @PathVariable("id") Long id,
            @RequestBody(required = false) AnalysisConfigDto config) {
        AnalyticsJob job = analysisOrchestratorService.startAnalysis(id, config);
        return ResponseEntity.ok(ApiResponse.success("Analysis started successfully", job));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get progress status of the latest analytics job for a dataset")
    public ResponseEntity<ApiResponse<AnalyticsJob>> getJobStatus(@PathVariable("id") Long id) {
        AnalyticsJob job = analyticsJobRepository.findFirstByDatasetIdOrderByStartedAtDesc(id)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(job));
    }
}

