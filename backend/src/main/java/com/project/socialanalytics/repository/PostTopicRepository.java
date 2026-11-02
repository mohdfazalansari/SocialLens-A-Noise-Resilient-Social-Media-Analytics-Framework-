package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.PostTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTopicRepository extends JpaRepository<PostTopic, Long> {
    List<PostTopic> findByPostId(Long postId);
    List<PostTopic> findByTopicId(Long topicId);
    void deleteByPostDatasetId(Long datasetId);
}

