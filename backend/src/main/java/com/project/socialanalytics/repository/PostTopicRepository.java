package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.PostTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTopicRepository extends JpaRepository<PostTopic, Long> {

    @Query("SELECT pt FROM PostTopic pt WHERE pt.post.id = :postId")
    List<PostTopic> findByPostId(@Param("postId") Long postId);

    @Query("SELECT pt FROM PostTopic pt WHERE pt.topic.id = :topicId")
    List<PostTopic> findByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT pt FROM PostTopic pt WHERE pt.post.id = :postId AND pt.topic.id = :topicId")
    Optional<PostTopic> findByPostIdAndTopicId(@Param("postId") Long postId, @Param("topicId") Long topicId);
}

