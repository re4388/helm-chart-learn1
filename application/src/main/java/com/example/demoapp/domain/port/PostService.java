package com.example.demoapp.domain.port;

import com.example.demoapp.domain.model.Post;
import java.util.List;
import java.util.Optional;

/**
 * Domain service interface for post operations
 * 文章操作的領域服務介面
 */
public interface PostService {
    
    /**
     * Create a new post
     * 建立新文章
     */
    Post createPost(String id, String title, String content);
    
    /**
     * Get post by ID
     * 根據 ID 取得文章
     */
    Optional<Post> getPostById(String id);
    
    /**
     * Get all posts
     * 取得所有文章
     */
    List<Post> getAllPosts();
    
    /**
     * Get published posts only
     * 只取得已發布的文章
     */
    List<Post> getPublishedPosts();
    
    /**
     * Publish a post
     * 發布文章
     */
    Post publishPost(String id);
    
    /**
     * Archive a post
     * 封存文章
     */
    Post archivePost(String id);
    
    /**
     * Update post content
     * 更新文章內容
     */
    Post updatePost(String id, String title, String content);
    
    /**
     * Delete a post
     * 刪除文章
     */
    void deletePost(String id);
}