package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.Post;
import com.example.demoapp.domain.port.PostService;
import org.springframework.stereotype.Service;

/**
 * Use case for managing posts (create, update, delete, publish)
 * 用例：管理文章（建立、更新、刪除、發布）
 */
@Service
public class ManagePostUseCase {
    
    private final PostService postService;
    
    public ManagePostUseCase(PostService postService) {
        this.postService = postService;
    }
    
    /**
     * Execute use case to create a new post
     * 執行用例以建立新文章
     */
    public Post createPost(String id, String title, String content) {
        validatePostData(id, title, content);
        return postService.createPost(id.trim(), title.trim(), content.trim());
    }
    
    /**
     * Execute use case to update a post
     * 執行用例以更新文章
     */
    public Post updatePost(String id, String title, String content) {
        validatePostData(id, title, content);
        return postService.updatePost(id.trim(), title.trim(), content.trim());
    }
    
    /**
     * Execute use case to publish a post
     * 執行用例以發布文章
     */
    public Post publishPost(String id) {
        validateId(id);
        return postService.publishPost(id.trim());
    }
    
    /**
     * Execute use case to archive a post
     * 執行用例以封存文章
     */
    public Post archivePost(String id) {
        validateId(id);
        return postService.archivePost(id.trim());
    }
    
    /**
     * Execute use case to delete a post
     * 執行用例以刪除文章
     */
    public void deletePost(String id) {
        validateId(id);
        postService.deletePost(id.trim());
    }
    
    private void validatePostData(String id, String title, String content) {
        validateId(id);
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be null or empty");
        }
    }
    
    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }
    }
}