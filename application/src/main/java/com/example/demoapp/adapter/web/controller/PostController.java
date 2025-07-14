package com.example.demoapp.adapter.web.controller;

import com.example.demoapp.adapter.web.dto.CreatePostRequest;
import com.example.demoapp.adapter.web.dto.PostResponse;
import com.example.demoapp.adapter.web.dto.UpdatePostRequest;
import com.example.demoapp.application.usecase.GetPostsUseCase;
import com.example.demoapp.application.usecase.ManagePostUseCase;
import com.example.demoapp.domain.model.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Post operations following hexagonal architecture
 * 遵循六角架構的文章操作 REST 控制器
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final GetPostsUseCase getPostsUseCase;
    private final ManagePostUseCase managePostUseCase;

    public PostController(GetPostsUseCase getPostsUseCase, ManagePostUseCase managePostUseCase) {
        this.getPostsUseCase = getPostsUseCase;
        this.managePostUseCase = managePostUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<Post> posts = getPostsUseCase.getAllPosts();
        List<PostResponse> responses = posts.stream()
            .map(PostResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/published")
    public ResponseEntity<List<PostResponse>> getPublishedPosts() {
        List<Post> posts = getPostsUseCase.getPublishedPosts();
        List<PostResponse> responses = posts.stream()
            .map(PostResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable String id) {
        return getPostsUseCase.getPostById(id)
            .map(post -> ResponseEntity.ok(PostResponse.fromDomain(post)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request) {
        try {
            Post post = managePostUseCase.createPost(request.getId(), request.getTitle(), request.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable String id, @RequestBody UpdatePostRequest request) {
        try {
            Post post = managePostUseCase.updatePost(id, request.getTitle(), request.getContent());
            return ResponseEntity.ok(PostResponse.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publishPost(@PathVariable String id) {
        try {
            Post post = managePostUseCase.publishPost(id);
            return ResponseEntity.ok(PostResponse.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<PostResponse> archivePost(@PathVariable String id) {
        try {
            Post post = managePostUseCase.archivePost(id);
            return ResponseEntity.ok(PostResponse.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        try {
            managePostUseCase.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}