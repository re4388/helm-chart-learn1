package com.example.demoapp.infrastructure.service;

import com.example.demoapp.domain.model.Post;
import com.example.demoapp.domain.port.PostRepository;
import com.example.demoapp.domain.port.PostService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PostService
 * PostService 的實作
 */
@Service
public class PostServiceImpl implements PostService {
    
    private final PostRepository postRepository;
    
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
    
    @Override
    public Post createPost(String id, String title, String content) {
        if (postRepository.existsById(id)) {
            throw new IllegalArgumentException("Post with ID " + id + " already exists");
        }
        Post post = Post.create(id, title, content);
        return postRepository.save(post);
    }
    
    @Override
    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }
    
    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    
    @Override
    public List<Post> getPublishedPosts() {
        return postRepository.findByStatus(Post.PostStatus.PUBLISHED);
    }
    
    @Override
    public Post publishPost(String id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));
        Post publishedPost = post.publish();
        return postRepository.save(publishedPost);
    }
    
    @Override
    public Post archivePost(String id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));
        Post archivedPost = post.archive();
        return postRepository.save(archivedPost);
    }
    
    @Override
    public Post updatePost(String id, String title, String content) {
        Post existingPost = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));
        
        Post updatedPost = new Post(id, title, content, existingPost.getStatus(), existingPost.getCreatedAt());
        return postRepository.save(updatedPost);
    }
    
    @Override
    public void deletePost(String id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("Post not found with ID: " + id);
        }
        postRepository.deleteById(id);
    }
}