package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.NetworkNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {

    @Query("SELECT n FROM NetworkNode n WHERE n.dataset.id = :datasetId ORDER BY n.pagerank DESC")
    List<NetworkNode> findByDatasetIdOrderByPagerankDesc(@Param("datasetId") Long datasetId, Pageable pageable);

    @Query("SELECT n FROM NetworkNode n WHERE n.dataset.id = :datasetId ORDER BY n.betweennessCentrality DESC")
    List<NetworkNode> findByDatasetIdOrderByBetweennessCentralityDesc(@Param("datasetId") Long datasetId, Pageable pageable);

    @Query("SELECT n FROM NetworkNode n WHERE n.dataset.id = :datasetId")
    List<NetworkNode> findByDatasetId(@Param("datasetId") Long datasetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NetworkNode n WHERE n.dataset.id = :datasetId")
    void deleteByDatasetId(@Param("datasetId") Long datasetId);
}

