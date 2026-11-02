package com.project.socialanalytics.service;

import com.project.socialanalytics.dto.InfluencerDto;
import com.project.socialanalytics.dto.NetworkGraphDto;
import com.project.socialanalytics.entity.NetworkEdge;
import com.project.socialanalytics.entity.NetworkNode;
import com.project.socialanalytics.exception.ResourceNotFoundException;
import com.project.socialanalytics.repository.DatasetRepository;
import com.project.socialanalytics.repository.NetworkEdgeRepository;
import com.project.socialanalytics.repository.NetworkNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NetworkService {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private NetworkEdgeRepository networkEdgeRepository;

    @Transactional(readOnly = true)
    public NetworkGraphDto getNetworkGraph(Long datasetId, int maxNodes) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        int limit = maxNodes > 0 ? Math.min(maxNodes, 200) : 100;
        List<NetworkNode> topNodes = networkNodeRepository.findByDatasetIdOrderByPagerankDesc(datasetId, PageRequest.of(0, limit));
        List<NetworkEdge> edges = networkEdgeRepository.findByDatasetId(datasetId);

        List<NetworkGraphDto.NodeDto> nodeDtos = topNodes.stream().map(n -> new NetworkGraphDto.NodeDto(
                n.getNodeId(),
                n.getPseudonym(),
                n.getInDegree() + n.getOutDegree(),
                n.getPagerank(),
                n.getBetweennessCentrality(),
                n.getCommunityId()
        )).collect(Collectors.toList());

        List<NetworkGraphDto.EdgeDto> edgeDtos = edges.stream().map(e -> new NetworkGraphDto.EdgeDto(
                e.getSourceNode(),
                e.getTargetNode(),
                e.getInteractionType(),
                e.getWeight()
        )).collect(Collectors.toList());

        int n = nodeDtos.size();
        int e = edgeDtos.size();
        double density = n > 1 ? (double) (2 * e) / (n * (n - 1)) : 0.0;

        NetworkGraphDto.NetworkMetricsDto metrics = new NetworkGraphDto.NetworkMetricsDto(
                n,
                e,
                Math.round(density * 10000.0) / 10000.0,
                n > 0 ? 1 : 0
        );

        NetworkGraphDto dto = new NetworkGraphDto();
        dto.setNodes(nodeDtos);
        dto.setEdges(edgeDtos);
        dto.setMetrics(metrics);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<InfluencerDto> getInfluencers(Long datasetId, int limit) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        int safeLimit = limit > 0 ? Math.min(limit, 50) : 20;
        List<NetworkNode> nodes = networkNodeRepository.findByDatasetIdOrderByPagerankDesc(datasetId, PageRequest.of(0, safeLimit));

        return nodes.stream().map(n -> new InfluencerDto(
                n.getNodeId(),
                n.getPseudonym(),
                n.getInDegree(),
                n.getOutDegree(),
                n.getPagerank(),
                n.getBetweennessCentrality(),
                n.getCommunityId()
        )).collect(Collectors.toList());
    }
}

