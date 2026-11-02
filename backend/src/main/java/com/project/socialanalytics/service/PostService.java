package com.project.socialanalytics.service;

import com.project.socialanalytics.dto.PagedResponse;
import com.project.socialanalytics.dto.PostDto;
import com.project.socialanalytics.entity.PostTopic;
import com.project.socialanalytics.entity.SocialPost;
import com.project.socialanalytics.exception.ResourceNotFoundException;
import com.project.socialanalytics.repository.EmotionResultRepository;
import com.project.socialanalytics.repository.PostTopicRepository;
import com.project.socialanalytics.repository.SentimentResultRepository;
import com.project.socialanalytics.repository.SocialPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Autowired
    private SentimentResultRepository sentimentResultRepository;

    @Autowired
    private EmotionResultRepository emotionResultRepository;

    @Autowired
    private PostTopicRepository postTopicRepository;

    @Transactional(readOnly = true)
    public PagedResponse<PostDto> getPosts(Long datasetId,
                                           String query,
                                           String platform,
                                           Boolean isDuplicate,
                                           int page,
                                           int size,
                                           String sortBy,
                                           String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SocialPost> postsPage = socialPostRepository.searchPosts(datasetId, query, platform, isDuplicate, pageable);

        List<PostDto> dtos = postsPage.getContent().stream()
                .map(this::toPostDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(dtos, postsPage.getNumber(), postsPage.getSize(),
                postsPage.getTotalElements(), postsPage.getTotalPages(), postsPage.isLast());
    }

    @Transactional(readOnly = true)
    public PostDto getPostById(Long id) {
        SocialPost post = socialPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SocialPost", "id", id));
        return toPostDto(post);
    }

    private PostDto toPostDto(SocialPost post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setDatasetId(post.getDataset().getId());
        dto.setPlatformPostId(post.getPlatformPostId());
        dto.setPlatform(post.getPlatform());
        dto.setAuthorId(post.getAuthorId());
        dto.setPostText(post.getPostText());
        dto.setCleanedText(post.getCleanedText());
        dto.setLanguage(post.getLanguage());
        dto.setLikeCount(post.getLikeCount());
        dto.setReplyCount(post.getReplyCount());
        dto.setShareCount(post.getShareCount());
        dto.setReplyToPostId(post.getReplyToPostId());
        dto.setIsDuplicate(post.getIsDuplicate());
        dto.setDuplicateGroupId(post.getDuplicateGroup() != null ? post.getDuplicateGroup().getId() : null);
        dto.setPostCreatedAt(post.getPostCreatedAt());

        sentimentResultRepository.findByPostId(post.getId()).ifPresent(s -> {
            dto.setSentimentLabel(s.getLabel());
            if ("POSITIVE".equalsIgnoreCase(s.getLabel())) {
                dto.setSentimentConfidence(s.getPositiveScore());
            } else if ("NEGATIVE".equalsIgnoreCase(s.getLabel())) {
                dto.setSentimentConfidence(s.getNegativeScore());
            } else {
                dto.setSentimentConfidence(s.getNeutralScore());
            }
        });

        emotionResultRepository.findByPostId(post.getId()).ifPresent(e -> {
            dto.setDominantEmotion(e.getDominantEmotion());
            dto.setEmotionConfidence(e.getEmotionConfidence());
        });

        List<PostTopic> pts = postTopicRepository.findByPostId(post.getId());
        if (!pts.isEmpty()) {
            dto.setTopics(pts.stream().map(pt -> pt.getTopic().getName()).collect(Collectors.toList()));
        }

        return dto;
    }
}

