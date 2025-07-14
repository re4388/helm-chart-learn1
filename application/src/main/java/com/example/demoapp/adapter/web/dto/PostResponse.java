package com.example.demoapp.adapter.web.dto;

import com.example.demoapp.domain.model.Post;
import java.time.LocalDateTime;

/**
 * Response DTO for post endpoints
 * 文章端點的回應 DTO
 */
public class PostResponse {
    private final String id;
    private final String title;
    private final String content;
    private final String status;
    private final LocalDateTime createdAt;

    public PostResponse(String id, String title, String content, String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PostResponse fromDomain(Post post) {
        return new PostResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getStatus().name(),
            post.getCreatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}