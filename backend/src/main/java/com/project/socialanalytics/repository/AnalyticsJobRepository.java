package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.AnalyticsJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsJobRepository extends JpaRepository<AnalyticsJob, Long> {

    @Query("SELECT j FROM AnalyticsJob j WHERE j.dataset.id = :datasetId ORDER BY j.startedAt DESC")
    List<AnalyticsJob> findByDatasetIdOrderByStartedAtDesc(@Param("datasetId") Long datasetId);

    @Query("SELECT j FROM AnalyticsJob j WHERE j.dataset.id = :datasetId ORDER BY j.startedAt DESC LIMIT 1")
    Optional<AnalyticsJob> findFirstByDatasetIdOrderByStartedAtDesc(@Param("datasetId") Long datasetId);
}

