package com.example.demoapp.adapter.in.web;

import com.example.demoapp.domain.dto.CreatePostRequestDTO;
import com.example.demoapp.domain.dto.PostResponseDTO;
import com.example.demoapp.domain.dto.UpdatePostRequestDTO;
import com.example.demoapp.application.usecase.GetPostsUseCase;
import com.example.demoapp.application.usecase.ManagePostUseCase;
import com.example.demoapp.domain.model.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Post operations following hexagonal architecture
 * 遵循六角架構的文章操作 REST 控制器
 */
@Tag(name = "文章管理", description = "提供文章的建立、查詢、更新、發布、歸檔和刪除操作")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final GetPostsUseCase getPostsUseCase;
    private final ManagePostUseCase managePostUseCase;

    public PostController(GetPostsUseCase getPostsUseCase, ManagePostUseCase managePostUseCase) {
        this.getPostsUseCase = getPostsUseCase;
        this.managePostUseCase = managePostUseCase;
    }

    @Operation(summary = "獲取所有文章", description = "返回所有文章的列表，無論其狀態如何。")
    @ApiResponse(responseCode = "200", description = "成功獲取文章列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        List<Post> posts = getPostsUseCase.getAllPosts();
        List<PostResponseDTO> responses = posts.stream()
            .map(PostResponseDTO::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "獲取已發布文章", description = "返回所有已發布文章的列表。")
    @ApiResponse(responseCode = "200", description = "成功獲取已發布文章列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @GetMapping("/published")
    public ResponseEntity<List<PostResponseDTO>> getPublishedPosts() {
        List<Post> posts = getPostsUseCase.getPublishedPosts();
        List<PostResponseDTO> responses = posts.stream()
            .map(PostResponseDTO::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "根據 ID 獲取文章", description = "根據文章 ID 返回單篇文章的詳細資訊。")
    @ApiResponse(responseCode = "200", description = "成功獲取文章",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "文章未找到")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostById(@Parameter(description = "文章 ID", required = true, example = "654c7b1d0b8f1a2b3c4d5e6f") @PathVariable String id) {
        return getPostsUseCase.getPostById(id)
            .map(post -> ResponseEntity.ok(PostResponseDTO.fromDomain(post)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "建立新文章", description = "建立一篇新文章。")
    @ApiResponse(responseCode = "201", description = "文章成功建立",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "無效的請求或文章已存在")
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody(description = "建立文章的請求主體", required = true) @org.springframework.web.bind.annotation.RequestBody CreatePostRequestDTO request) {
        try {
            Post post = managePostUseCase.createPost(request.getId(), request.getTitle(), request.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(PostResponseDTO.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "更新文章", description = "根據文章 ID 更新文章的標題和內容。")
    @ApiResponse(responseCode = "200", description = "文章成功更新",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "文章未找到")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @Parameter(description = "文章 ID", required = true, example = "654c7b1d0b8f1a2b3c4d5e6f") @PathVariable String id,
            @RequestBody(description = "更新文章的請求主體", required = true) @org.springframework.web.bind.annotation.RequestBody UpdatePostRequestDTO request) {
        try {
            Post post = managePostUseCase.updatePost(id, request.getTitle(), request.getContent());
            return ResponseEntity.ok(PostResponseDTO.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "發布文章", description = "將指定 ID 的文章狀態設定為 PUBLISHED。")
    @ApiResponse(responseCode = "200", description = "文章成功發布",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "文章未找到")
    @PutMapping("/{id}/publish")
    public ResponseEntity<PostResponseDTO> publishPost(@Parameter(description = "要發布的文章 ID", required = true, example = "654c7b1d0b8f1a2b3c4d5e6f") @PathVariable String id) {
        try {
            Post post = managePostUseCase.publishPost(id);
            return ResponseEntity.ok(PostResponseDTO.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "歸檔文章", description = "將指定 ID 的文章狀態設定為 ARCHIVED。")
    @ApiResponse(responseCode = "200", description = "文章成功歸檔",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "文章未找到")
    @PutMapping("/{id}/archive")
    public ResponseEntity<PostResponseDTO> archivePost(@Parameter(description = "要歸檔的文章 ID", required = true, example = "654c7b1d0b8f1a2b3c4d5e6f") @PathVariable String id) {
        try {
            Post post = managePostUseCase.archivePost(id);
            return ResponseEntity.ok(PostResponseDTO.fromDomain(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "刪除文章", description = "根據文章 ID 刪除文章。")
    @ApiResponse(responseCode = "204", description = "文章成功刪除")
    @ApiResponse(responseCode = "404", description = "文章未找到")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@Parameter(description = "要刪除的文章 ID", required = true, example = "654c7b1d0b8f1a2b3c4d5e6f") @PathVariable String id) {
        try {
            managePostUseCase.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}