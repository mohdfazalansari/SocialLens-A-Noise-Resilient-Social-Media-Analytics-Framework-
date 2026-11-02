package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.AnalyticsJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsJobRepository extends JpaRepository<AnalyticsJob, Long> {
    List<AnalyticsJob> findByDatasetIdOrderByStartedAtDesc(Long datasetId);
    Optional<AnalyticsJob> findFirstByDatasetIdOrderByStartedAtDesc(Long datasetId);
}

