package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.SentimentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SentimentResultRepository extends JpaRepository<SentimentResult, Long> {

    Optional<SentimentResult> findByPostId(Long postId);

    @Query("SELECT s.label, COUNT(s) FROM SentimentResult s WHERE s.post.dataset.id = :datasetId GROUP BY s.label")
    List<Object[]> countSentimentDistribution(@Param("datasetId") Long datasetId);

    @Query("SELECT FUNCTION('DATE', p.postCreatedAt) as d, s.label, COUNT(s) " +
           "FROM SentimentResult s JOIN s.post p " +
           "WHERE p.dataset.id = :datasetId AND p.postCreatedAt IS NOT NULL " +
           "GROUP BY FUNCTION('DATE', p.postCreatedAt), s.label " +
           "ORDER BY d ASC")
    List<Object[]> getSentimentOverTime(@Param("datasetId") Long datasetId);
}

