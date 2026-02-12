package com.example.demoapp.domain.dto;

import com.example.demoapp.domain.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response DTO for post endpoints
 * 文章端點的回應 DTO
 */
@Schema(description = "文章回應物件")
public class PostResponse {
    @Schema(description = "文章 ID", example = "post-123")
    private final String id;
    @Schema(description = "文章標題", example = "我的第一篇文章")
    private final String title;
    @Schema(description = "文章內容", example = "這是一篇關於 Spring Boot 的文章。")
    private final String content;
    @Schema(description = "文章狀態 (DRAFT, PUBLISHED, ARCHIVED)", example = "PUBLISHED")
    private final String status;
    @Schema(description = "文章建立時間", example = "2023-10-26T10:00:00")
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