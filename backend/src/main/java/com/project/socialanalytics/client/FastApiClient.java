package com.project.socialanalytics.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FastApiClient {

    private static final Logger logger = LoggerFactory.getLogger(FastApiClient.class);

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public FastApiClient(RestTemplate restTemplate,
                         @Value("${sociallens.ai-service.url:http://localhost:8000}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(aiServiceUrl + "/health", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("AI Service health check failed: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> preprocessPosts(List<Map<String, Object>> posts) {
        String url = aiServiceUrl + "/ai/preprocess";
        return postForMap(url, posts);
    }

    public Map<String, Object> detectDuplicates(List<Map<String, Object>> posts, double similarityThreshold) {
        String url = aiServiceUrl + "/ai/detect-duplicates";
        Map<String, Object> payload = new HashMap<>();
        payload.put("posts", posts);
        payload.put("similarity_threshold", similarityThreshold);
        return postForMap(url, payload);
    }

    public List<Map<String, Object>> batchSentiment(List<Map<String, Object>> texts) {
        String url = aiServiceUrl + "/ai/sentiment";
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", texts);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            logger.error("FastAPI /ai/sentiment call failed: {}", e.getMessage());
            throw new RuntimeException("AI sentiment service error: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> batchEmotions(List<Map<String, Object>> texts) {
        String url = aiServiceUrl + "/ai/emotions";
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", texts);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            logger.error("FastAPI /ai/emotions call failed: {}", e.getMessage());
            throw new RuntimeException("AI emotions service error: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> discoverTopics(List<Map<String, Object>> posts, int minTopicSize) {
        String url = aiServiceUrl + "/ai/topics/discover";
        Map<String, Object> payload = new HashMap<>();
        payload.put("posts", posts);
        payload.put("min_topic_size", minTopicSize);
        return postForMap(url, payload);
    }

    public Map<String, Object> detectTrends(Map<String, Object> trendRequest) {
        String url = aiServiceUrl + "/ai/trends/detect";
        return postForMap(url, trendRequest);
    }

    public Map<String, Object> analyzeNetwork(List<Map<String, Object>> interactions) {
        String url = aiServiceUrl + "/ai/network/analyze";
        Map<String, Object> payload = new HashMap<>();
        payload.put("interactions", interactions);
        return postForMap(url, payload);
    }

    public Map<String, Object> runEvaluation(Map<String, Object> evalRequest) {
        String url = aiServiceUrl + "/ai/evaluation/run";
        return postForMap(url, evalRequest);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postForMap(String url, Object payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody() != null ? (Map<String, Object>) response.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            logger.error("FastAPI call failed to URL {}: {}", url, e.getMessage());
            throw new RuntimeException("FastAPI service error: " + e.getMessage(), e);
        }
    }
}

