package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.NetworkEdge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NetworkEdgeRepository extends JpaRepository<NetworkEdge, Long> {

    @Query("SELECT e FROM NetworkEdge e WHERE e.dataset.id = :datasetId")
    List<NetworkEdge> findByDatasetId(@Param("datasetId") Long datasetId);

    @Query("SELECT e FROM NetworkEdge e WHERE e.dataset.id = :datasetId ORDER BY e.weight DESC")
    List<NetworkEdge> findByDatasetIdOrderByWeightDesc(@Param("datasetId") Long datasetId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM NetworkEdge e WHERE e.dataset.id = :datasetId")
    void deleteByDatasetId(@Param("datasetId") Long datasetId);
}

