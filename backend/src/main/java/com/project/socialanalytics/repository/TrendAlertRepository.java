package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.TrendAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendAlertRepository extends JpaRepository<TrendAlert, Long> {
    List<TrendAlert> findByDatasetId(Long datasetId);
    List<TrendAlert> findByDatasetIdOrderByTrendScoreDesc(Long datasetId);
    List<TrendAlert> findByDatasetIdAndAlertLevel(Long datasetId, String alertLevel);
    void deleteByDatasetId(Long datasetId);
}
