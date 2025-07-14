package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.Post;
import com.example.demoapp.domain.port.PostService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Use case for getting posts
 * 用例：取得文章
 */
@Service
public class GetPostsUseCase {
    
    private final PostService postService;
    
    public GetPostsUseCase(PostService postService) {
        this.postService = postService;
    }
    
    /**
     * Execute use case to get all posts
     * 執行用例以取得所有文章
     */
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }
    
    /**
     * Execute use case to get published posts only
     * 執行用例以取得已發布的文章
     */
    public List<Post> getPublishedPosts() {
        return postService.getPublishedPosts();
    }
    
    /**
     * Execute use case to get post by ID
     * 執行用例以根據 ID 取得文章
     */
    public Optional<Post> getPostById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }
        return postService.getPostById(id.trim());
    }
}