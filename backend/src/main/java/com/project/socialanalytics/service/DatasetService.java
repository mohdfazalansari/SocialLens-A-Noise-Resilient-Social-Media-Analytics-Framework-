package com.project.socialanalytics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.socialanalytics.dto.DatasetDetailDto;
import com.project.socialanalytics.dto.DatasetPreviewDto;
import com.project.socialanalytics.dto.DatasetSummaryDto;
import com.project.socialanalytics.dto.PagedResponse;
import com.project.socialanalytics.entity.Dataset;
import com.project.socialanalytics.entity.SocialPost;
import com.project.socialanalytics.entity.User;
import com.project.socialanalytics.exception.BadRequestException;
import com.project.socialanalytics.exception.ResourceNotFoundException;
import com.project.socialanalytics.repository.DatasetRepository;
import com.project.socialanalytics.repository.SocialPostRepository;
import com.project.socialanalytics.repository.UserRepository;
import com.project.socialanalytics.security.UserPrincipal;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Autowired
    private UserRepository userRepository;

    public DatasetPreviewDto previewFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BadRequestException("Invalid filename");
        }

        try {
            if (filename.toLowerCase().endsWith(".csv")) {
                return previewCsv(file);
            } else if (filename.toLowerCase().endsWith(".json")) {
                return previewJson(file);
            } else {
                throw new BadRequestException("Unsupported file type. Only CSV and JSON files are supported.");
            }
        } catch (Exception e) {
            logger.error("Error previewing file: {}", e.getMessage());
            throw new BadRequestException("Failed to preview dataset: " + e.getMessage(), e);
        }
    }

    private DatasetPreviewDto previewCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            List<String> headers = csvParser.getHeaderNames();
            List<Map<String, String>> sampleRows = new ArrayList<>();
            int count = 0;

            for (CSVRecord record : csvParser) {
                if (count < 10) {
                    Map<String, String> row = new HashMap<>();
                    for (String header : headers) {
                        row.put(header, record.isSet(header) ? record.get(header) : "");
                    }
                    sampleRows.add(row);
                }
                count++;
            }

            Map<String, String> mappings = suggestMappings(headers);
            return new DatasetPreviewDto(headers, sampleRows, count, mappings);
        }
    }

    private DatasetPreviewDto previewJson(MultipartFile file) throws Exception {
        List<Map<String, Object>> records = objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<>() {}
        );

        if (records.isEmpty()) {
            return new DatasetPreviewDto(Collections.emptyList(), Collections.emptyList(), 0, Collections.emptyMap());
        }

        List<String> headers = new ArrayList<>(records.get(0).keySet());
        List<Map<String, String>> sampleRows = new ArrayList<>();

        for (int i = 0; i < Math.min(10, records.size()); i++) {
            Map<String, Object> rec = records.get(i);
            Map<String, String> row = new HashMap<>();
            for (String h : headers) {
                row.put(h, rec.get(h) != null ? String.valueOf(rec.get(h)) : "");
            }
            sampleRows.add(row);
        }

        Map<String, String> mappings = suggestMappings(headers);
        return new DatasetPreviewDto(headers, sampleRows, records.size(), mappings);
    }

    private Map<String, String> suggestMappings(List<String> headers) {
        Map<String, String> mapping = new HashMap<>();
        for (String h : headers) {
            String lower = h.toLowerCase().trim();
            if (lower.contains("text") || lower.contains("content") || lower.contains("body") || lower.contains("tweet") || lower.contains("message")) {
                mapping.putIfAbsent("text", h);
            } else if (lower.contains("author") || lower.contains("user") || lower.contains("creator") || lower.contains("sender")) {
                mapping.putIfAbsent("author_id", h);
            } else if (lower.contains("post_id") || lower.contains("id") || lower.contains("tweet_id")) {
                mapping.putIfAbsent("post_id", h);
            } else if (lower.contains("platform") || lower.contains("source")) {
                mapping.putIfAbsent("platform", h);
            } else if (lower.contains("date") || lower.contains("time") || lower.contains("created")) {
                mapping.putIfAbsent("created_at", h);
            } else if (lower.contains("like") || lower.contains("favorite")) {
                mapping.putIfAbsent("like_count", h);
            } else if (lower.contains("reply") || lower.contains("comment")) {
                mapping.putIfAbsent("reply_count", h);
            } else if (lower.contains("share") || lower.contains("retweet") || lower.contains("repost")) {
                mapping.putIfAbsent("share_count", h);
            } else if (lower.contains("reply_to")) {
                mapping.putIfAbsent("reply_to_post_id", h);
            }
        }
        return mapping;
    }

    @Transactional
    public DatasetDetailDto importDataset(MultipartFile file,
                                          String name,
                                          String description,
                                          Map<String, String> columnMapping,
                                          UserPrincipal currentUser) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        String filename = file.getOriginalFilename();
        String format = filename != null && filename.toLowerCase().endsWith(".json") ? "JSON" : "CSV";

        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setDescription(description);
        dataset.setFileFormat(format);
        dataset.setAnalysisStatus(Dataset.AnalysisStatus.PENDING);
        dataset.setUploadedBy(user);

        Dataset savedDataset = datasetRepository.save(dataset);

        List<SocialPost> posts = parseAndCreatePosts(file, savedDataset, columnMapping, format);
        socialPostRepository.saveAll(posts);

        savedDataset.setTotalRecords(posts.size());
        datasetRepository.save(savedDataset);

        return getDatasetDetails(savedDataset.getId());
    }

    private List<SocialPost> parseAndCreatePosts(MultipartFile file,
                                                 Dataset dataset,
                                                 Map<String, String> mapping,
                                                 String format) {
        List<SocialPost> posts = new ArrayList<>();
        String textCol = mapping.getOrDefault("text", "text");
        String authorCol = mapping.getOrDefault("author_id", "author_id");
        String postIdCol = mapping.getOrDefault("post_id", "post_id");
        String platformCol = mapping.getOrDefault("platform", "platform");
        String dateCol = mapping.getOrDefault("created_at", "created_at");
        String likeCol = mapping.getOrDefault("like_count", "like_count");
        String replyCol = mapping.getOrDefault("reply_count", "reply_count");
        String shareCol = mapping.getOrDefault("share_count", "share_count");
        String replyToCol = mapping.getOrDefault("reply_to_post_id", "reply_to_post_id");

        try {
            if ("CSV".equals(format)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                     CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

                    for (CSVRecord rec : csvParser) {
                        String postText = getVal(rec, textCol);
                        if (postText == null || postText.trim().isEmpty()) continue;

                        SocialPost post = new SocialPost();
                        post.setDataset(dataset);
                        post.setPostText(postText);
                        post.setCleanedText(postText);
                        post.setAuthorId(getVal(rec, authorCol, "user_" + (posts.size() + 1)));
                        post.setPlatformPostId(getVal(rec, postIdCol, UUID.randomUUID().toString()));
                        post.setPlatform(getVal(rec, platformCol, "TWITTER").toUpperCase());
                        post.setLikeCount(parseInt(getVal(rec, likeCol), 0));
                        post.setReplyCount(parseInt(getVal(rec, replyCol), 0));
                        post.setShareCount(parseInt(getVal(rec, shareCol), 0));
                        post.setReplyToPostId(getVal(rec, replyToCol));
                        post.setPostCreatedAt(parseDateTime(getVal(rec, dateCol)));
                        post.setLanguage("en");
                        posts.add(post);
                    }
                }
            } else {
                List<Map<String, Object>> records = objectMapper.readValue(
                        file.getInputStream(),
                        new TypeReference<>() {}
                );
                for (Map<String, Object> rec : records) {
                    Object t = rec.get(textCol);
                    if (t == null || t.toString().trim().isEmpty()) continue;

                    SocialPost post = new SocialPost();
                    post.setDataset(dataset);
                    post.setPostText(t.toString());
                    post.setCleanedText(t.toString());
                    post.setAuthorId(rec.get(authorCol) != null ? rec.get(authorCol).toString() : "user_" + (posts.size() + 1));
                    post.setPlatformPostId(rec.get(postIdCol) != null ? rec.get(postIdCol).toString() : UUID.randomUUID().toString());
                    post.setPlatform(rec.get(platformCol) != null ? rec.get(platformCol).toString().toUpperCase() : "TWITTER");
                    post.setLikeCount(parseInt(rec.get(likeCol), 0));
                    post.setReplyCount(parseInt(rec.get(replyCol), 0));
                    post.setShareCount(parseInt(rec.get(shareCol), 0));
                    post.setReplyToPostId(rec.get(replyToCol) != null ? rec.get(replyToCol).toString() : null);
                    post.setPostCreatedAt(parseDateTime(rec.get(dateCol) != null ? rec.get(dateCol).toString() : null));
                    post.setLanguage("en");
                    posts.add(post);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing posts: {}", e.getMessage());
            throw new BadRequestException("Data format parsing failed: " + e.getMessage(), e);
        }

        return posts;
    }

    private String getVal(CSVRecord rec, String col) {
        return col != null && rec.isSet(col) ? rec.get(col) : null;
    }

    private String getVal(CSVRecord rec, String col, String def) {
        String v = getVal(rec, col);
        return v != null && !v.trim().isEmpty() ? v.trim() : def;
    }

    private int parseInt(Object val, int def) {
        if (val == null) return def;
        try {
            return Integer.parseInt(val.toString().trim().replaceAll("[^0-9-]", ""));
        } catch (Exception e) {
            return def;
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        String clean = raw.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        for (DateTimeFormatter fmt : formatters) {
            try {
                if (clean.length() <= 10) {
                    return LocalDateTime.parse(clean + "T00:00:00");
                }
                return LocalDateTime.parse(clean, fmt);
            } catch (DateTimeParseException ignored) {}
        }

        return LocalDateTime.now();
    }

    @Transactional(readOnly = true)
    public PagedResponse<DatasetSummaryDto> getAllDatasets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Dataset> datasets = datasetRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<DatasetSummaryDto> dtos = datasets.getContent().stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(dtos, datasets.getNumber(), datasets.getSize(),
                datasets.getTotalElements(), datasets.getTotalPages(), datasets.isLast());
    }

    @Transactional(readOnly = true)
    public DatasetDetailDto getDatasetDetails(Long id) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", "id", id));

        DatasetDetailDto dto = new DatasetDetailDto();
        dto.setId(dataset.getId());
        dto.setName(dataset.getName());
        dto.setDescription(dataset.getDescription());
        dto.setFileFormat(dataset.getFileFormat());
        dto.setTotalRecords(dataset.getTotalRecords());
        dto.setAnalysisStatus(dataset.getAnalysisStatus().name());
        dto.setUploadedByUsername(dataset.getUploadedBy() != null ? dataset.getUploadedBy().getUsername() : "Anonymous");
        dto.setCreatedAt(dataset.getCreatedAt());
        dto.setUpdatedAt(dataset.getUpdatedAt());

        long unique = socialPostRepository.countByDatasetIdAndIsDuplicateFalse(id);
        long dup = socialPostRepository.countByDatasetIdAndIsDuplicateTrue(id);
        dto.setUniqueRecords(unique);
        dto.setDuplicateRecords(dup);

        Map<String, Long> platforms = new HashMap<>();
        for (Object[] row : socialPostRepository.countByPlatformForDataset(id)) {
            platforms.put((String) row[0], (Long) row[1]);
        }
        dto.setPlatformDistribution(platforms);

        Map<String, Long> languages = new HashMap<>();
        for (Object[] row : socialPostRepository.countByLanguageForDataset(id)) {
            languages.put((String) row[0], (Long) row[1]);
        }
        dto.setLanguageDistribution(languages);

        return dto;
    }

    @Transactional
    public void deleteDataset(Long id) {
        if (!datasetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dataset", "id", id);
        }
        datasetRepository.deleteById(id);
    }

    private DatasetSummaryDto toSummaryDto(Dataset dataset) {
        DatasetSummaryDto dto = new DatasetSummaryDto();
        dto.setId(dataset.getId());
        dto.setName(dataset.getName());
        dto.setDescription(dataset.getDescription());
        dto.setFileFormat(dataset.getFileFormat());
        dto.setTotalRecords(dataset.getTotalRecords());
        dto.setAnalysisStatus(dataset.getAnalysisStatus().name());
        dto.setUploadedByUsername(dataset.getUploadedBy() != null ? dataset.getUploadedBy().getUsername() : "Anonymous");
        dto.setCreatedAt(dataset.getCreatedAt());
        return dto;
    }
}

