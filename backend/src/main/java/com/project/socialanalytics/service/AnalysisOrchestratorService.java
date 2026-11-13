package com.project.socialanalytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.socialanalytics.client.FastApiClient;
import com.project.socialanalytics.dto.AnalysisConfigDto;
import com.project.socialanalytics.entity.*;
import com.project.socialanalytics.exception.ResourceNotFoundException;
import com.project.socialanalytics.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalysisOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisOrchestratorService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Autowired
    private DuplicateGroupRepository duplicateGroupRepository;

    @Autowired
    private SentimentResultRepository sentimentResultRepository;

    @Autowired
    private EmotionResultRepository emotionResultRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private PostTopicRepository postTopicRepository;

    @Autowired
    private TrendSnapshotRepository trendSnapshotRepository;

    @Autowired
    private TrendAlertRepository trendAlertRepository;

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private NetworkEdgeRepository networkEdgeRepository;

    @Autowired
    private AnalyticsJobRepository analyticsJobRepository;

    @Autowired
    private FastApiClient fastApiClient;

    public AnalyticsJob startAnalysis(Long datasetId, AnalysisConfigDto config) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", "id", datasetId));

        dataset.setAnalysisStatus(Dataset.AnalysisStatus.PROCESSING);
        datasetRepository.save(dataset);

        AnalyticsJob job = new AnalyticsJob();
        job.setDataset(dataset);
        job.setJobType("FULL_PIPELINE");
        job.setStatus(AnalyticsJob.JobStatus.RUNNING);
        job.setProgressPercentage(5);
        job.setStartedAt(LocalDateTime.now());
        AnalyticsJob savedJob = analyticsJobRepository.save(job);

        CompletableFuture.runAsync(() -> runPipeline(savedJob.getId(), datasetId, config));

        return savedJob;
    }

    public void runPipeline(Long jobId, Long datasetId, AnalysisConfigDto config) {
        logger.info("Starting AI pipeline execution for datasetId: {}", datasetId);
        try {
            updateJobProgress(jobId, 10, "Fetching dataset posts...");
            List<SocialPost> posts = socialPostRepository.findByDatasetId(datasetId);

            if (posts.isEmpty()) {
                markJobFailed(jobId, datasetId, "Dataset contains no posts to analyze.");
                return;
            }

            // Step 1: Preprocessing & Duplicate Detection
            updateJobProgress(jobId, 25, "Running duplicate and near-duplicate detection...");
            List<Map<String, Object>> rawPostsForAI = new ArrayList<>();
            for (SocialPost p : posts) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("text", p.getPostText());
                m.put("author_id", p.getAuthorId());
                m.put("created_at", p.getPostCreatedAt() != null ? p.getPostCreatedAt().toString() : LocalDateTime.now().toString());
                m.put("like_count", p.getLikeCount());
                m.put("reply_count", p.getReplyCount());
                m.put("share_count", p.getShareCount());
                m.put("reply_to_post_id", p.getReplyToPostId());
                rawPostsForAI.add(m);
            }

            double similarityThreshold = config != null && config.getDuplicateSimilarityThreshold() != null
                    ? config.getDuplicateSimilarityThreshold() : 0.85;

            Map<String, Object> dupResult = fastApiClient.detectDuplicates(rawPostsForAI, similarityThreshold);
            processDuplicates(posts, dupResult);

            // Step 2: Sentiment and Emotion Inference
            updateJobProgress(jobId, 45, "Executing RoBERTa sentiment and DistilRoBERTa emotion inference...");
            List<Map<String, Object>> textBatch = new ArrayList<>();
            for (SocialPost p : posts) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("text", p.getCleanedText() != null ? p.getCleanedText() : p.getPostText());
                textBatch.add(m);
            }

            List<Map<String, Object>> sentimentResults = fastApiClient.batchSentiment(textBatch);
            processSentiments(posts, sentimentResults);

            List<Map<String, Object>> emotionResults = fastApiClient.batchEmotions(textBatch);
            processEmotions(posts, emotionResults);

            // Step 3: BERTopic Topic Discovery
            updateJobProgress(jobId, 65, "Discovering semantic topics using BERTopic & Sentence-Transformers...");
            int minTopicSize = config != null && config.getMinTopicSize() != null ? config.getMinTopicSize() : 3;
            Map<String, Object> topicResult = fastApiClient.discoverTopics(rawPostsForAI, minTopicSize);
            Map<Integer, Topic> topicMap = processTopics(datasetId, posts, topicResult);

            // Step 4: Noise-Resilient Trend Detection
            updateJobProgress(jobId, 80, "Calculating noise-resilient trend anomalies & baseline comparisons...");
            Map<String, Object> trendRequest = new HashMap<>();
            trendRequest.put("dataset_id", datasetId);
            trendRequest.put("posts", buildTrendPostsPayload(posts, topicMap));
            trendRequest.put("alpha", config != null ? config.getTrendAlpha() : 0.40);
            trendRequest.put("beta", config != null ? config.getTrendBeta() : 0.30);
            trendRequest.put("gamma", config != null ? config.getTrendGamma() : 0.20);
            trendRequest.put("delta", config != null ? config.getTrendDelta() : 0.10);
            trendRequest.put("window_size_hours", config != null && config.getWindowSizeHours() != null ? config.getWindowSizeHours() : 24);

            Map<String, Object> trendResult = fastApiClient.detectTrends(trendRequest);
            processTrends(datasetId, topicMap, trendResult);

            // Step 5: NetworkX Social Interaction Graph Analysis
            updateJobProgress(jobId, 92, "Constructing social interaction graph and computing PageRank...");
            List<Map<String, Object>> interactions = new ArrayList<>();
            for (SocialPost p : posts) {
                if (p.getReplyToPostId() != null && !p.getReplyToPostId().isEmpty()) {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("source", p.getAuthorId());
                    edge.put("target", p.getReplyToPostId());
                    edge.put("type", "REPLY");
                    interactions.add(edge);
                }
            }
            Map<String, Object> networkResult = fastApiClient.analyzeNetwork(interactions);
            processNetwork(datasetId, networkResult);

            // Completion
            updateJobProgress(jobId, 100, "Analytics pipeline completed successfully.");
            markJobCompleted(jobId, datasetId);
            logger.info("Successfully finished AI pipeline for datasetId: {}", datasetId);

        } catch (Exception e) {
            logger.error("AI pipeline execution error on datasetId {}: {}", datasetId, e.getMessage(), e);
            markJobFailed(jobId, datasetId, e.getMessage());
        }
    }

    @Transactional
    public void processDuplicates(List<SocialPost> posts, Map<String, Object> dupResult) {
        if (!dupResult.containsKey("clusters")) return;

        Map<Long, SocialPost> postMap = new HashMap<>();
        for (SocialPost p : posts) {
            postMap.put(p.getId(), p);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clusters = (List<Map<String, Object>>) dupResult.get("clusters");
        for (Map<String, Object> cluster : clusters) {
            String hash = (String) cluster.get("cluster_hash");
            String type = (String) cluster.getOrDefault("duplicate_type", "EXACT");
            double sim = ((Number) cluster.getOrDefault("similarity", 1.0)).doubleValue();
            Long canonicalId = ((Number) cluster.get("canonical_id")).longValue();

            @SuppressWarnings("unchecked")
            List<Number> memberIds = (List<Number>) cluster.get("member_ids");

            DuplicateGroup group = duplicateGroupRepository.findByGroupHash(hash)
                    .orElseGet(() -> new DuplicateGroup(hash, type, BigDecimal.valueOf(sim), canonicalId, memberIds.size()));
            group.setDuplicateType(type);
            group.setSimilarityScore(BigDecimal.valueOf(sim));
            group.setCanonicalPostId(canonicalId);
            group.setMemberCount(memberIds.size());
            DuplicateGroup savedGroup = duplicateGroupRepository.save(group);

            for (Number numId : memberIds) {
                Long postId = numId.longValue();
                SocialPost p = postMap.get(postId);
                if (p != null) {
                    p.setDuplicateGroup(savedGroup);
                    if (!postId.equals(canonicalId)) {
                        p.setIsDuplicate(true);
                    }
                    socialPostRepository.save(p);
                }
            }
        }
    }

    @Transactional
    public void processSentiments(List<SocialPost> posts, List<Map<String, Object>> results) {
        Map<Long, SocialPost> postMap = new HashMap<>();
        for (SocialPost p : posts) postMap.put(p.getId(), p);

        for (Map<String, Object> r : results) {
            Long postId = ((Number) r.get("id")).longValue();
            SocialPost post = postMap.get(postId);
            if (post != null) {
                SentimentResult sr = sentimentResultRepository.findByPostId(postId).orElse(new SentimentResult());
                sr.setPost(post);
                sr.setLabel((String) r.get("label"));
                sr.setPositiveScore(toBigDecimal(r.get("positive_score")));
                sr.setNegativeScore(toBigDecimal(r.get("negative_score")));
                sr.setNeutralScore(toBigDecimal(r.get("neutral_score")));
                sr.setModelName((String) r.getOrDefault("model_name", "twitter-roberta-base-sentiment-latest"));
                sentimentResultRepository.save(sr);
            }
        }
    }

    @Transactional
    public void processEmotions(List<SocialPost> posts, List<Map<String, Object>> results) {
        Map<Long, SocialPost> postMap = new HashMap<>();
        for (SocialPost p : posts) postMap.put(p.getId(), p);

        for (Map<String, Object> r : results) {
            Long postId = ((Number) r.get("id")).longValue();
            SocialPost post = postMap.get(postId);
            if (post != null) {
                EmotionResult er = emotionResultRepository.findByPostId(postId).orElse(new EmotionResult());
                er.setPost(post);
                er.setDominantEmotion((String) r.get("dominant_emotion"));
                er.setEmotionConfidence(toBigDecimal(r.get("confidence")));
                er.setModelName((String) r.getOrDefault("model_name", "emotion-english-distilroberta-base"));
                try {
                    er.setEmotionScores(objectMapper.writeValueAsString(r.get("scores")));
                } catch (Exception ignored) {}
                emotionResultRepository.save(er);
            }
        }
    }

    @Transactional
    public Map<Integer, Topic> processTopics(Long datasetId, List<SocialPost> posts, Map<String, Object> topicResult) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow();
        Map<Long, SocialPost> postMap = new HashMap<>();
        for (SocialPost p : posts) postMap.put(p.getId(), p);

        Map<Integer, Topic> topicMap = new HashMap<>();
        if (!topicResult.containsKey("topics")) return topicMap;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topicList = (List<Map<String, Object>>) topicResult.get("topics");
        for (Map<String, Object> t : topicList) {
            int idx = ((Number) t.get("topic_index")).intValue();
            String name = (String) t.get("name");
            String keywords = (String) t.getOrDefault("keywords", "");
            int count = ((Number) t.getOrDefault("count", 0)).intValue();
            double coherence = ((Number) t.getOrDefault("coherence", 0.0)).doubleValue();

            Topic topic = topicRepository.findByDatasetIdAndTopicIndex(datasetId, idx).orElse(new Topic());
            topic.setDataset(dataset);
            topic.setTopicIndex(idx);
            topic.setName(name);
            topic.setTopKeywords(keywords);
            topic.setPostCount(count);
            topic.setRepresentativeCoherence(BigDecimal.valueOf(coherence));
            Topic saved = topicRepository.save(topic);
            topicMap.put(idx, saved);
        }

        // Post-Topic assignments
        if (topicResult.containsKey("assignments")) {
            Set<String> seenPairs = new HashSet<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> assignments = (List<Map<String, Object>>) topicResult.get("assignments");
            for (Map<String, Object> a : assignments) {
                Long postId = ((Number) a.get("post_id")).longValue();
                int tIdx = ((Number) a.get("topic_index")).intValue();
                double prob = ((Number) a.getOrDefault("probability", 1.0)).doubleValue();

                SocialPost post = postMap.get(postId);
                Topic topic = topicMap.get(tIdx);
                if (post != null && topic != null) {
                    String pairKey = postId + "-" + topic.getId();
                    if (seenPairs.add(pairKey)) {
                        PostTopic pt = postTopicRepository.findByPostIdAndTopicId(postId, topic.getId())
                                .orElseGet(() -> new PostTopic(post, topic, BigDecimal.valueOf(prob)));
                        pt.setProbability(BigDecimal.valueOf(prob));
                        postTopicRepository.save(pt);
                    }
                }
            }
        }

        return topicMap;
    }

    @Transactional
    public void processTrends(Long datasetId, Map<Integer, Topic> topicMap, Map<String, Object> trendResult) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow();
        trendAlertRepository.deleteByDatasetId(datasetId);
        trendSnapshotRepository.deleteByDatasetId(datasetId);

        // Save snapshots
        if (trendResult.containsKey("snapshots")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> snaps = (List<Map<String, Object>>) trendResult.get("snapshots");
            for (Map<String, Object> s : snaps) {
                int tIdx = ((Number) s.get("topic_index")).intValue();
                Topic topic = topicMap.get(tIdx);
                if (topic == null) continue;

                TrendSnapshot snapshot = new TrendSnapshot();
                snapshot.setDataset(dataset);
                snapshot.setTopic(topic);
                snapshot.setWindowStart(parseDate(s.get("window_start")));
                snapshot.setWindowEnd(parseDate(s.get("window_end")));
                snapshot.setRawPostCount(((Number) s.getOrDefault("raw_post_count", 0)).intValue());
                snapshot.setUniquePostCount(((Number) s.getOrDefault("unique_post_count", 0)).intValue());
                snapshot.setDuplicateRatio(toBigDecimal(s.get("duplicate_ratio")));
                snapshot.setGrowthRate(toBigDecimal(s.get("growth_rate")));
                snapshot.setZScore(toBigDecimal(s.get("z_score")));
                snapshot.setSentimentShift(toBigDecimal(s.get("sentiment_shift")));
                snapshot.setTrendScore(toBigDecimal(s.get("trend_score")));
                snapshot.setClassification((String) s.getOrDefault("classification", "STABLE"));
                trendSnapshotRepository.save(snapshot);
            }
        }

        // Save alerts
        if (trendResult.containsKey("alerts")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alerts = (List<Map<String, Object>>) trendResult.get("alerts");
            for (Map<String, Object> a : alerts) {
                int tIdx = ((Number) a.get("topic_index")).intValue();
                Topic topic = topicMap.get(tIdx);
                if (topic == null) continue;

                TrendAlert alert = new TrendAlert();
                alert.setDataset(dataset);
                alert.setTopic(topic);
                alert.setTopicName(topic.getName());
                alert.setAlertLevel((String) a.getOrDefault("alert_level", "MEDIUM"));
                alert.setClassification((String) a.getOrDefault("classification", "EMERGING"));
                alert.setTrendScore(toBigDecimal(a.get("trend_score")));
                alert.setBaselineVolume(toBigDecimal(a.get("baseline_volume")));
                alert.setCurrentVolume(toBigDecimal(a.get("current_volume")));
                alert.setGrowthPercentage(toBigDecimal(a.get("growth_percentage")));
                alert.setSentimentShift(toBigDecimal(a.get("sentiment_shift")));
                alert.setDuplicateRatio(toBigDecimal(a.get("duplicate_ratio")));
                alert.setAlertReason((String) a.get("alert_reason"));

                if (a.containsKey("supporting_post_ids")) {
                    try {
                        alert.setSupportingPostIds(objectMapper.writeValueAsString(a.get("supporting_post_ids")));
                    } catch (Exception ignored) {}
                }
                trendAlertRepository.save(alert);
            }
        }
    }

    @Transactional
    public void processNetwork(Long datasetId, Map<String, Object> networkResult) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow();
        networkEdgeRepository.deleteByDatasetId(datasetId);
        networkNodeRepository.deleteByDatasetId(datasetId);

        if (networkResult.containsKey("nodes")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) networkResult.get("nodes");
            for (Map<String, Object> n : nodes) {
                NetworkNode node = new NetworkNode();
                node.setDataset(dataset);
                node.setNodeId((String) n.get("node_id"));
                node.setPseudonym((String) n.getOrDefault("pseudonym", "user_" + n.get("node_id")));
                node.setInDegree(((Number) n.getOrDefault("in_degree", 0)).intValue());
                node.setOutDegree(((Number) n.getOrDefault("out_degree", 0)).intValue());
                node.setBetweennessCentrality(toBigDecimal(n.get("betweenness")));
                node.setPagerank(toBigDecimal(n.get("pagerank")));
                node.setCommunityId(((Number) n.getOrDefault("community", 0)).intValue());
                networkNodeRepository.save(node);
            }
        }

        if (networkResult.containsKey("edges")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edges = (List<Map<String, Object>>) networkResult.get("edges");
            for (Map<String, Object> e : edges) {
                NetworkEdge edge = new NetworkEdge(
                        dataset,
                        (String) e.get("source"),
                        (String) e.get("target"),
                        (String) e.getOrDefault("type", "REPLY"),
                        ((Number) e.getOrDefault("weight", 1)).intValue()
                );
                networkEdgeRepository.save(edge);
            }
        }
    }

    private List<Map<String, Object>> buildTrendPostsPayload(List<SocialPost> posts, Map<Integer, Topic> topicMap) {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (SocialPost p : posts) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("author_id", p.getAuthorId());
            m.put("created_at", p.getPostCreatedAt() != null ? p.getPostCreatedAt().toString() : LocalDateTime.now().toString());
            m.put("is_duplicate", p.getIsDuplicate());
            m.put("likes", p.getLikeCount());
            m.put("replies", p.getReplyCount());
            m.put("shares", p.getShareCount());

            sentimentResultRepository.findByPostId(p.getId()).ifPresent(s -> {
                m.put("sentiment", s.getLabel());
                m.put("sentiment_pos", s.getPositiveScore());
                m.put("sentiment_neg", s.getNegativeScore());
            });

            List<PostTopic> pts = postTopicRepository.findByPostId(p.getId());
            if (!pts.isEmpty()) {
                m.put("topic_index", pts.get(0).getTopic().getTopicIndex());
            } else {
                m.put("topic_index", -1);
            }

            payload.add(m);
        }
        return payload;
    }

    private void updateJobProgress(Long jobId, int progress, String message) {
        analyticsJobRepository.findById(jobId).ifPresent(j -> {
            j.setProgressPercentage(progress);
            analyticsJobRepository.save(j);
        });
    }

    private void markJobCompleted(Long jobId, Long datasetId) {
        analyticsJobRepository.findById(jobId).ifPresent(j -> {
            j.setStatus(AnalyticsJob.JobStatus.COMPLETED);
            j.setProgressPercentage(100);
            j.setCompletedAt(LocalDateTime.now());
            analyticsJobRepository.save(j);
        });
        datasetRepository.findById(datasetId).ifPresent(d -> {
            d.setAnalysisStatus(Dataset.AnalysisStatus.COMPLETED);
            datasetRepository.save(d);
        });
    }

    private void markJobFailed(Long jobId, Long datasetId, String err) {
        analyticsJobRepository.findById(jobId).ifPresent(j -> {
            j.setStatus(AnalyticsJob.JobStatus.FAILED);
            j.setErrorMessage(err);
            j.setCompletedAt(LocalDateTime.now());
            analyticsJobRepository.save(j);
        });
        datasetRepository.findById(datasetId).ifPresent(d -> {
            d.setAnalysisStatus(Dataset.AnalysisStatus.FAILED);
            datasetRepository.save(d);
        });
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDateTime parseDate(Object obj) {
        if (obj == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(obj.toString());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}

