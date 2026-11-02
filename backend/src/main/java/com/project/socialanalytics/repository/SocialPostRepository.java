package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.SocialPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long>, JpaSpecificationExecutor<SocialPost> {

    List<SocialPost> findByDatasetId(Long datasetId);

    Page<SocialPost> findByDatasetId(Long datasetId, Pageable pageable);

    long countByDatasetId(Long datasetId);

    long countByDatasetIdAndIsDuplicateFalse(Long datasetId);

    long countByDatasetIdAndIsDuplicateTrue(Long datasetId);

    @Query("SELECT p.platform, COUNT(p) FROM SocialPost p WHERE p.dataset.id = :datasetId GROUP BY p.platform")
    List<Object[]> countByPlatformForDataset(@Param("datasetId") Long datasetId);

    @Query("SELECT p.language, COUNT(p) FROM SocialPost p WHERE p.dataset.id = :datasetId GROUP BY p.language")
    List<Object[]> countByLanguageForDataset(@Param("datasetId") Long datasetId);

    @Query("SELECT p FROM SocialPost p WHERE p.dataset.id = :datasetId AND " +
           "(:query IS NULL OR LOWER(p.postText) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:platform IS NULL OR p.platform = :platform) AND " +
           "(:isDuplicate IS NULL OR p.isDuplicate = :isDuplicate)")
    Page<SocialPost> searchPosts(@Param("datasetId") Long datasetId,
                                 @Param("query") String query,
                                 @Param("platform") String platform,
                                 @Param("isDuplicate") Boolean isDuplicate,
                                 Pageable pageable);
}

