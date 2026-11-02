package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.TrendSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendSnapshotRepository extends JpaRepository<TrendSnapshot, Long> {
    List<TrendSnapshot> findByDatasetIdOrderByWindowStartAsc(Long datasetId);
    List<TrendSnapshot> findByTopicIdOrderByWindowStartAsc(Long topicId);
    void deleteByDatasetId(Long datasetId);
}

