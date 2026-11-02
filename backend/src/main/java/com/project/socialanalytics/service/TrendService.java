package com.project.socialanalytics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.socialanalytics.dto.TrendAlertDto;
import com.project.socialanalytics.dto.TrendAnalyticsDto;
import com.project.socialanalytics.entity.TrendAlert;
import com.project.socialanalytics.entity.TrendSnapshot;
import com.project.socialanalytics.exception.ResourceNotFoundException;
import com.project.socialanalytics.repository.DatasetRepository;
import com.project.socialanalytics.repository.TrendAlertRepository;
import com.project.socialanalytics.repository.TrendSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrendService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private TrendSnapshotRepository trendSnapshotRepository;

    @Autowired
    private TrendAlertRepository trendAlertRepository;

    @Transactional(readOnly = true)
    public TrendAnalyticsDto getTrendAnalytics(Long datasetId, String method) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        String selectedMethod = (method == null || method.trim().isEmpty()) ? "PROPOSED_NOISE_RESILIENT" : method.toUpperCase();

        List<TrendSnapshot> snapshots = trendSnapshotRepository.findByDatasetIdOrderByWindowStartAsc(datasetId);
        List<TrendAlert> alerts = trendAlertRepository.findByDatasetIdOrderByTrendScoreDesc(datasetId);

        TrendAnalyticsDto dto = new TrendAnalyticsDto();
        dto.setMethod(selectedMethod);

        dto.setSnapshots(snapshots.stream().map(s -> {
            TrendAnalyticsDto.TrendSnapshotDto snap = new TrendAnalyticsDto.TrendSnapshotDto();
            snap.setId(s.getId());
            snap.setTopicId(s.getTopic().getId());
            snap.setTopicName(s.getTopic().getName());
            snap.setWindowStart(s.getWindowStart());
            snap.setWindowEnd(s.getWindowEnd());
            snap.setRawPostCount(s.getRawPostCount());
            snap.setUniquePostCount(s.getUniquePostCount());
            snap.setDuplicateRatio(s.getDuplicateRatio());
            snap.setGrowthRate(s.getGrowthRate());
            snap.setZScore(s.getZScore());
            snap.setSentimentShift(s.getSentimentShift());
            snap.setTrendScore(s.getTrendScore());
            snap.setClassification(s.getClassification());
            return snap;
        }).collect(Collectors.toList()));

        dto.setAlerts(alerts.stream().map(this::toAlertDto).collect(Collectors.toList()));

        return dto;
    }

    @Transactional(readOnly = true)
    public TrendAlertDto getAlertById(Long alertId) {
        TrendAlert alert = trendAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("TrendAlert", "id", alertId));
        return toAlertDto(alert);
    }

    private TrendAlertDto toAlertDto(TrendAlert a) {
        TrendAlertDto dto = new TrendAlertDto();
        dto.setId(a.getId());
        dto.setDatasetId(a.getDataset().getId());
        dto.setTopicId(a.getTopic().getId());
        dto.setTopicName(a.getTopicName());
        dto.setAlertLevel(a.getAlertLevel());
        dto.setClassification(a.getClassification());
        dto.setTrendScore(a.getTrendScore());
        dto.setBaselineVolume(a.getBaselineVolume());
        dto.setCurrentVolume(a.getCurrentVolume());
        dto.setGrowthPercentage(a.getGrowthPercentage());
        dto.setSentimentShift(a.getSentimentShift());
        dto.setDuplicateRatio(a.getDuplicateRatio());
        dto.setAlertReason(a.getAlertReason());
        dto.setDetectedAt(a.getDetectedAt());

        if (a.getSupportingPostIds() != null && !a.getSupportingPostIds().isEmpty()) {
            try {
                List<Long> ids = objectMapper.readValue(a.getSupportingPostIds(), new TypeReference<>() {});
                dto.setSupportingPostIds(ids);
            } catch (Exception ignored) {
                dto.setSupportingPostIds(Collections.emptyList());
            }
        } else {
            dto.setSupportingPostIds(Collections.emptyList());
        }

        return dto;
    }
}

