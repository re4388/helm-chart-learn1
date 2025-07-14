package com.example.demoapp.adapter.web.dto;

/**
 * Request DTO for creating posts
 * 建立文章的請求 DTO
 */
public class CreatePostRequest {
    private String id;
    private String title;
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