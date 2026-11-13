package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.TrendSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrendSnapshotRepository extends JpaRepository<TrendSnapshot, Long> {

    @Query("SELECT s FROM TrendSnapshot s WHERE s.dataset.id = :datasetId ORDER BY s.windowStart ASC")
    List<TrendSnapshot> findByDatasetIdOrderByWindowStartAsc(@Param("datasetId") Long datasetId);

    @Query("SELECT s FROM TrendSnapshot s WHERE s.topic.id = :topicId ORDER BY s.windowStart ASC")
    List<TrendSnapshot> findByTopicIdOrderByWindowStartAsc(@Param("topicId") Long topicId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TrendSnapshot s WHERE s.dataset.id = :datasetId")
    void deleteByDatasetId(@Param("datasetId") Long datasetId);
}

