package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.EmotionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionResultRepository extends JpaRepository<EmotionResult, Long> {

    Optional<EmotionResult> findByPostId(Long postId);

    @Query("SELECT e.dominantEmotion, COUNT(e) FROM EmotionResult e WHERE e.post.dataset.id = :datasetId GROUP BY e.dominantEmotion")
    List<Object[]> countEmotionDistribution(@Param("datasetId") Long datasetId);
}

