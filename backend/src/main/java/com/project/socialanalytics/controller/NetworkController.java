package com.project.socialanalytics.controller;

import com.project.socialanalytics.dto.ApiResponse;
import com.project.socialanalytics.dto.InfluencerDto;
import com.project.socialanalytics.dto.NetworkGraphDto;
import com.project.socialanalytics.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/network")
@Tag(name = "Network", description = "Social interaction graph topology, centrality metrics, and PageRank")
public class NetworkController {

    @Autowired
    private NetworkService networkService;

    @GetMapping("/graph")
    @Operation(summary = "Get graph nodes, edges, and density metrics for visual representation")
    public ResponseEntity<ApiResponse<NetworkGraphDto>> getNetworkGraph(
            @RequestParam("datasetId") Long datasetId,
            @RequestParam(value = "maxNodes", defaultValue = "100") int maxNodes) {
        NetworkGraphDto graph = networkService.getNetworkGraph(datasetId, maxNodes);
        return ResponseEntity.ok(ApiResponse.success(graph));
    }

    @GetMapping("/influencers")
    @Operation(summary = "Get top influential accounts ranked by PageRank and Betweenness Centrality")
    public ResponseEntity<ApiResponse<List<InfluencerDto>>> getInfluencers(
            @RequestParam("datasetId") Long datasetId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<InfluencerDto> influencers = networkService.getInfluencers(datasetId, limit);
        return ResponseEntity.ok(ApiResponse.success(influencers));
    }
}

