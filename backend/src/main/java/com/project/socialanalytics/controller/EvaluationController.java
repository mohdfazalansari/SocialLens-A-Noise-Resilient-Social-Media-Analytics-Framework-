package com.project.socialanalytics.controller;

import com.project.socialanalytics.dto.ApiResponse;
import com.project.socialanalytics.dto.EvaluationReportDto;
import com.project.socialanalytics.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@Tag(name = "Evaluation", description = "Research benchmarking: Precision, Recall, F1, and Detection Delay comparisons")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/run")
    @Operation(summary = "Run experimental evaluation comparing Baselines vs Proposed method")
    public ResponseEntity<ApiResponse<EvaluationReportDto>> runEvaluation(
            @RequestParam("datasetId") Long datasetId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "groundTruthSource", required = false) String groundTruthSource) {
        EvaluationReportDto report = evaluationService.runEvaluation(datasetId, name, groundTruthSource);
        return ResponseEntity.ok(ApiResponse.success("Evaluation executed successfully", report));
    }

    @GetMapping("/runs")
    @Operation(summary = "Get historical evaluation experiment reports for a dataset")
    public ResponseEntity<ApiResponse<List<EvaluationReportDto>>> getEvaluationRuns(@RequestParam("datasetId") Long datasetId) {
        List<EvaluationReportDto> reports = evaluationService.getEvaluationRuns(datasetId);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
}

