package com.example.demoapp.adapter.web.dto;

/**
 * Request DTO for updating posts
 * 更新文章的請求 DTO
 */
public class UpdatePostRequest {
    private String title;
    private String content;

    // Default constructor for JSON deserialization
    public UpdatePostRequest() {}

    public UpdatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
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