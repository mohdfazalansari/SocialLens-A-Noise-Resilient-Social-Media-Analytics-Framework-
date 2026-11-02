package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.EvaluationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRunRepository extends JpaRepository<EvaluationRun, Long> {
    List<EvaluationRun> findByDatasetIdOrderByExecutedAtDesc(Long datasetId);
}

