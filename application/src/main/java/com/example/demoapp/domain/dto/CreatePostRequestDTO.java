package com.example.demoapp.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for creating posts
 * 建立文章的請求 DTO
 */
@Schema(description = "建立文章請求物件")
public class CreatePostRequest {
    @Schema(description = "文章 ID (可選，如果未提供則自動生成)", example = "post-123")
    private String id;
    @Schema(description = "文章標題", required = true, example = "我的第一篇文章")
    private String title;
    @Schema(description = "文章內容", required = true, example = "這是一篇關於 Spring Boot 的文章。")
    private String content;

    // Default constructor for JSON deserialization
    public CreatePostRequest() {}

    public CreatePostRequest(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}