package com.example.demoapp.domain.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing a blog post
 * 領域實體：部落格文章
 */
public class Post {
    private final String id;
    private final String title;
    private final String content;
    private final PostStatus status;
    private final LocalDateTime createdAt;

    public enum PostStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public Post(String id, String title, String content, PostStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Post create(String id, String title, String content) {
        return new Post(id, title, content, PostStatus.DRAFT, LocalDateTime.now());
    }

    public Post publish() {
        return new Post(this.id, this.title, this.content, PostStatus.PUBLISHED, this.createdAt);
    }

    public Post archive() {
        return new Post(this.id, this.title, this.content, PostStatus.ARCHIVED, this.createdAt);
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

    public PostStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isPublished() {
        return status == PostStatus.PUBLISHED;
    }
}