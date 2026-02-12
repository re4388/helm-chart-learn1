package com.example.demoapp.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for updating posts
 * 更新文章的請求 DTO
 */
@Schema(description = "更新文章請求物件")
public class UpdatePostRequestDTO {
    @Schema(description = "文章標題", example = "更新後的文章標題")
    private String title;
    @Schema(description = "文章內容", example = "這是更新後的文章內容。")
    private String content;

    // Default constructor for JSON deserialization
    public UpdatePostRequestDTO() {}

    public UpdatePostRequestDTO(String title, String content) {
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