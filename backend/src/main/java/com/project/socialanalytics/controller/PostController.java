package com.project.socialanalytics.controller;

import com.project.socialanalytics.dto.ApiResponse;
import com.project.socialanalytics.dto.PagedResponse;
import com.project.socialanalytics.dto.PostDto;
import com.project.socialanalytics.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Explore and search posts, filter by duplicate status, sentiment, and platform")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    @Operation(summary = "Search and filter social media posts for a dataset")
    public ResponseEntity<ApiResponse<PagedResponse<PostDto>>> getPosts(
            @RequestParam("datasetId") Long datasetId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "isDuplicate", required = false) Boolean isDuplicate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "postCreatedAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        PagedResponse<PostDto> posts = postService.getPosts(datasetId, query, platform, isDuplicate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single post by ID with enriched sentiment and topic annotations")
    public ResponseEntity<ApiResponse<PostDto>> getPostById(@PathVariable("id") Long id) {
        PostDto post = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
}

