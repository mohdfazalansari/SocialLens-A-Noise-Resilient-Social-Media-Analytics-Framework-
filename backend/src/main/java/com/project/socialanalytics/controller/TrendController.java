package com.project.socialanalytics.controller;

import com.project.socialanalytics.dto.ApiResponse;
import com.project.socialanalytics.dto.TrendAlertDto;
import com.project.socialanalytics.dto.TrendAnalyticsDto;
import com.project.socialanalytics.service.TrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trends")
@Tag(name = "Trends", description = "Noise-resilient trend detection, anomaly scoring, and emerging alerts")
public class TrendController {

    @Autowired
    private TrendService trendService;

    @GetMapping
    @Operation(summary = "Get trend snapshots and alerts, switchable by detection method")
    public ResponseEntity<ApiResponse<TrendAnalyticsDto>> getTrends(
            @RequestParam("datasetId") Long datasetId,
            @RequestParam(value = "method", defaultValue = "PROPOSED_NOISE_RESILIENT") String method) {
        TrendAnalyticsDto trends = trendService.getTrendAnalytics(datasetId, method);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get deep-dive evidence and supporting post IDs for a specific trend alert")
    public ResponseEntity<ApiResponse<TrendAlertDto>> getAlertById(@PathVariable("alertId") Long alertId) {
        TrendAlertDto alert = trendService.getAlertById(alertId);
        return ResponseEntity.ok(ApiResponse.success(alert));
    }
}

