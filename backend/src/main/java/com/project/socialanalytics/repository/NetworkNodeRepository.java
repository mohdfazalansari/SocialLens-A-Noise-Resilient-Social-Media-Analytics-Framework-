package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.NetworkNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {
    List<NetworkNode> findByDatasetIdOrderByPagerankDesc(Long datasetId, Pageable pageable);
    List<NetworkNode> findByDatasetIdOrderByBetweennessCentralityDesc(Long datasetId, Pageable pageable);
    List<NetworkNode> findByDatasetId(Long datasetId);
    void deleteByDatasetId(Long datasetId);
}

