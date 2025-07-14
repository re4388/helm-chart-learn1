package com.example.demoapp.domain.port;

import com.example.demoapp.domain.model.Post;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Post entity (outbound port)
 * Post 實體的儲存庫介面（輸出埠）
 */
public interface PostRepository {
    
    /**
     * Save a post
     * 儲存文章
     */
    Post save(Post post);
    
    /**
     * Find post by ID
     * 根據 ID 尋找文章
     */
    Optional<Post> findById(String id);
    
    /**
     * Find all posts
     * 尋找所有文章
     */
    List<Post> findAll();
    
    /**
     * Find posts by status
     * 根據狀態尋找文章
     */
    List<Post> findByStatus(Post.PostStatus status);
    
    /**
     * Delete post by ID
     * 根據 ID 刪除文章
     */
    void deleteById(String id);
    
    /**
     * Check if post exists by ID
     * 檢查文章是否存在
     */
    boolean existsById(String id);
}