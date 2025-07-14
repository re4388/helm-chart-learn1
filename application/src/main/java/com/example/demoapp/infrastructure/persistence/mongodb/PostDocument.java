package com.example.demoapp.infrastructure.persistence.mongodb;

import com.example.demoapp.domain.model.Post;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * MongoDB document for Post entity
 * Post 實體的 MongoDB 文件
 */
@Document(collection = "posts")
public class PostDocument {
    
    @Id
    private String id;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    
    // Default constructor for MongoDB
    public PostDocument() {}
    
    public PostDocument(String id, String title, String content, String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    /**
     * Convert from domain model to document
     * 從領域模型轉換為文件
     */
    public static PostDocument fromDomain(Post post) {
        return new PostDocument(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getStatus().name(),
            post.getCreatedAt()
        );
    }
    
    /**
     * Convert from document to domain model
     * 從文件轉換為領域模型
     */
    public Post toDomain() {
        Post.PostStatus postStatus;
        try {
            postStatus = Post.PostStatus.valueOf(this.status);
        } catch (IllegalArgumentException e) {
            postStatus = Post.PostStatus.DRAFT; // Default fallback
        }
        
        return new Post(this.id, this.title, this.content, postStatus, this.createdAt);
    }
    
    // Getters and setters
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}