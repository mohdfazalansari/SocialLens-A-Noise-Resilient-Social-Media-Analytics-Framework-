package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    Page<Dataset> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Dataset> findByUploadedById(Long userId);
}

