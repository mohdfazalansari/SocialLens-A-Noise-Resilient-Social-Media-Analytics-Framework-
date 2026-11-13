package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.TrendAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrendAlertRepository extends JpaRepository<TrendAlert, Long> {

    @Query("SELECT a FROM TrendAlert a WHERE a.dataset.id = :datasetId")
    List<TrendAlert> findByDatasetId(@Param("datasetId") Long datasetId);

    @Query("SELECT a FROM TrendAlert a WHERE a.dataset.id = :datasetId ORDER BY a.trendScore DESC")
    List<TrendAlert> findByDatasetIdOrderByTrendScoreDesc(@Param("datasetId") Long datasetId);

    @Query("SELECT a FROM TrendAlert a WHERE a.dataset.id = :datasetId AND a.alertLevel = :alertLevel")
    List<TrendAlert> findByDatasetIdAndAlertLevel(@Param("datasetId") Long datasetId, @Param("alertLevel") String alertLevel);

    @Modifying
    @Transactional
    @Query("DELETE FROM TrendAlert a WHERE a.dataset.id = :datasetId")
    void deleteByDatasetId(@Param("datasetId") Long datasetId);
}
