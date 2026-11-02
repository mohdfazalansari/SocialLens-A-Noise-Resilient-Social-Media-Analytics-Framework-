package com.project.socialanalytics.controller;

import com.project.socialanalytics.dto.*;
import com.project.socialanalytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Aggregated insights on sentiment, emotions, topics, and volume")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/overview")
    @Operation(summary = "Get high-level summary metrics and counts for the dashboard")
    public ResponseEntity<ApiResponse<AnalyticsOverviewDto>> getOverview(@RequestParam("datasetId") Long datasetId) {
        AnalyticsOverviewDto overview = analyticsService.getOverview(datasetId);
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/sentiment")
    @Operation(summary = "Get sentiment distribution breakdown and timeline series")
    public ResponseEntity<ApiResponse<SentimentAnalyticsDto>> getSentiment(@RequestParam("datasetId") Long datasetId) {
        SentimentAnalyticsDto sentiment = analyticsService.getSentimentAnalytics(datasetId);
        return ResponseEntity.ok(ApiResponse.success(sentiment));
    }

    @GetMapping("/emotions")
    @Operation(summary = "Get 7-class emotion classification distribution")
    public ResponseEntity<ApiResponse<EmotionAnalyticsDto>> getEmotions(@RequestParam("datasetId") Long datasetId) {
        EmotionAnalyticsDto emotions = analyticsService.getEmotionAnalytics(datasetId);
        return ResponseEntity.ok(ApiResponse.success(emotions));
    }

    @GetMapping("/topics")
    @Operation(summary = "Get discovered BERTopic semantic clusters and keyword representations")
    public ResponseEntity<ApiResponse<List<TopicAnalyticsDto>>> getTopics(@RequestParam("datasetId") Long datasetId) {
        List<TopicAnalyticsDto> topics = analyticsService.getTopicAnalytics(datasetId);
        return ResponseEntity.ok(ApiResponse.success(topics));
    }
}

