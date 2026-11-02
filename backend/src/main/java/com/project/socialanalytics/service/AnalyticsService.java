package com.project.socialanalytics.service;

import com.project.socialanalytics.dto.AnalyticsOverviewDto;
import com.project.socialanalytics.dto.EmotionAnalyticsDto;
import com.project.socialanalytics.dto.SentimentAnalyticsDto;
import com.project.socialanalytics.dto.TopicAnalyticsDto;
import com.project.socialanalytics.entity.Topic;
import com.project.socialanalytics.exception.ResourceNotFoundException;
import com.project.socialanalytics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Autowired
    private SentimentResultRepository sentimentResultRepository;

    @Autowired
    private EmotionResultRepository emotionResultRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TrendAlertRepository trendAlertRepository;

    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview(Long datasetId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        AnalyticsOverviewDto dto = new AnalyticsOverviewDto();
        long total = socialPostRepository.countByDatasetId(datasetId);
        long unique = socialPostRepository.countByDatasetIdAndIsDuplicateFalse(datasetId);
        long dup = socialPostRepository.countByDatasetIdAndIsDuplicateTrue(datasetId);

        dto.setTotalPosts(total);
        dto.setUniquePosts(unique);
        dto.setDuplicatePosts(dup);

        long pos = 0, neg = 0, neu = 0;
        List<Object[]> sentDist = sentimentResultRepository.countSentimentDistribution(datasetId);
        for (Object[] row : sentDist) {
            String label = (String) row[0];
            Long count = (Long) row[1];
            if ("POSITIVE".equalsIgnoreCase(label)) pos = count;
            else if ("NEGATIVE".equalsIgnoreCase(label)) neg = count;
            else neu = count;
        }

        dto.setPositivePosts(pos);
        dto.setNegativePosts(neg);
        dto.setNeutralPosts(neu);

        if (pos + neg > 0) {
            double polarity = (double) (pos - neg) / (pos + neg);
            dto.setAverageSentimentPolarity(Math.round(polarity * 100.0) / 100.0);
        }

        List<Topic> topics = topicRepository.findByDatasetIdOrderByPostCountDesc(datasetId);
        dto.setActiveTopics(topics.size());

        List<com.project.socialanalytics.entity.TrendAlert> alerts = trendAlertRepository.findByDatasetId(datasetId);
        long emergingCount = alerts.stream().filter(a -> "EMERGING".equalsIgnoreCase(a.getClassification())).count();
        dto.setEmergingTrends((int) emergingCount);

        Map<String, Long> platforms = new HashMap<>();
        for (Object[] r : socialPostRepository.countByPlatformForDataset(datasetId)) {
            platforms.put((String) r[0], (Long) r[1]);
        }
        dto.setPlatformBreakdown(platforms);

        Map<String, Long> emotions = new HashMap<>();
        for (Object[] r : emotionResultRepository.countEmotionDistribution(datasetId)) {
            emotions.put((String) r[0], (Long) r[1]);
        }
        dto.setEmotionBreakdown(emotions);

        return dto;
    }

    @Transactional(readOnly = true)
    public SentimentAnalyticsDto getSentimentAnalytics(Long datasetId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        SentimentAnalyticsDto dto = new SentimentAnalyticsDto();
        Map<String, Long> dist = new HashMap<>();
        dist.put("POSITIVE", 0L);
        dist.put("NEGATIVE", 0L);
        dist.put("NEUTRAL", 0L);

        List<Object[]> rows = sentimentResultRepository.countSentimentDistribution(datasetId);
        long total = 0;
        for (Object[] row : rows) {
            String label = ((String) row[0]).toUpperCase();
            Long count = (Long) row[1];
            dist.put(label, count);
            total += count;
        }
        dto.setDistribution(dist);

        long pos = dist.getOrDefault("POSITIVE", 0L);
        long neg = dist.getOrDefault("NEGATIVE", 0L);
        if (total > 0) {
            dto.setNetSentimentScore(Math.round(((double) (pos - neg) / total) * 1000.0) / 1000.0);
        } else {
            dto.setNetSentimentScore(0.0);
        }

        // Sentiment over time
        Map<String, long[]> timeMap = new TreeMap<>();
        List<Object[]> timeRows = sentimentResultRepository.getSentimentOverTime(datasetId);
        for (Object[] r : timeRows) {
            String dateStr = r[0] != null ? r[0].toString() : "Unknown";
            String label = ((String) r[1]).toUpperCase();
            Long count = (Long) r[2];

            timeMap.putIfAbsent(dateStr, new long[3]); // [0]=pos, [1]=neg, [2]=neu
            if ("POSITIVE".equals(label)) timeMap.get(dateStr)[0] += count;
            else if ("NEGATIVE".equals(label)) timeMap.get(dateStr)[1] += count;
            else timeMap.get(dateStr)[2] += count;
        }

        List<SentimentAnalyticsDto.TimePoint> timePoints = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : timeMap.entrySet()) {
            timePoints.add(new SentimentAnalyticsDto.TimePoint(
                    entry.getKey(),
                    entry.getValue()[0],
                    entry.getValue()[1],
                    entry.getValue()[2]
            ));
        }
        dto.setOverTime(timePoints);

        return dto;
    }

    @Transactional(readOnly = true)
    public EmotionAnalyticsDto getEmotionAnalytics(Long datasetId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        EmotionAnalyticsDto dto = new EmotionAnalyticsDto();
        Map<String, Long> dist = new HashMap<>();
        List<Object[]> rows = emotionResultRepository.countEmotionDistribution(datasetId);

        String topEmotion = "NEUTRAL";
        long maxCount = -1;
        long total = 0;

        for (Object[] row : rows) {
            String emotion = ((String) row[0]).toUpperCase();
            Long count = (Long) row[1];
            dist.put(emotion, count);
            total += count;
            if (count > maxCount) {
                maxCount = count;
                topEmotion = emotion;
            }
        }

        dto.setDistribution(dist);
        dto.setPrimaryEmotion(topEmotion);
        dto.setPrimaryEmotionPercentage(total > 0 ? Math.round(((double) maxCount / total) * 1000.0) / 10.0 : 0.0);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<TopicAnalyticsDto> getTopicAnalytics(Long datasetId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset", "id", datasetId);
        }

        List<Topic> topics = topicRepository.findByDatasetIdOrderByPostCountDesc(datasetId);

        return topics.stream().map(t -> {
            TopicAnalyticsDto dto = new TopicAnalyticsDto();
            dto.setId(t.getId());
            dto.setTopicIndex(t.getTopicIndex());
            dto.setName(t.getName());
            dto.setPostCount(t.getPostCount());
            dto.setRepresentativeCoherence(t.getRepresentativeCoherence());

            if (t.getTopKeywords() != null && !t.getTopKeywords().isEmpty()) {
                dto.setTopKeywords(Arrays.asList(t.getTopKeywords().split(",")));
            } else {
                dto.setTopKeywords(Collections.emptyList());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}

