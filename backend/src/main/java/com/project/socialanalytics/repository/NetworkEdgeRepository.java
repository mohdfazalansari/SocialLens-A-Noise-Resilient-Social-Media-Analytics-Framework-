package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.NetworkEdge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkEdgeRepository extends JpaRepository<NetworkEdge, Long> {
    List<NetworkEdge> findByDatasetId(Long datasetId);
    List<NetworkEdge> findByDatasetIdOrderByWeightDesc(Long datasetId, Pageable pageable);
    void deleteByDatasetId(Long datasetId);
}

