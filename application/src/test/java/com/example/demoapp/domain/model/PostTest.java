package com.example.demoapp.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    void shouldCreatePost() {
        // Given
        String id = "post1";
        String title = "My First Post";
        String content = "This is the content of my first post.";
        
        // When
        Post post = Post.create(id, title, content);
        
        // Then
        assertEquals(id, post.getId());
        assertEquals(title, post.getTitle());
        assertEquals(content, post.getContent());
        assertEquals(Post.PostStatus.DRAFT, post.getStatus());
        assertNotNull(post.getCreatedAt());
        assertFalse(post.isPublished());
    }

    @Test
    void shouldPublishPost() {
        // Given
        Post draftPost = Post.create("post1", "Title", "Content");
        
        // When
        Post publishedPost = draftPost.publish();
        
        // Then
        assertEquals(Post.PostStatus.PUBLISHED, publishedPost.getStatus());
        assertTrue(publishedPost.isPublished());
        assertEquals(draftPost.getId(), publishedPost.getId());
        assertEquals(draftPost.getTitle(), publishedPost.getTitle());
        assertEquals(draftPost.getContent(), publishedPost.getContent());
    }

    @Test
    void shouldArchivePost() {
        // Given
        Post post = Post.create("post1", "Title", "Content");
        
        // When
        Post archivedPost = post.archive();
        
        // Then
        assertEquals(Post.PostStatus.ARCHIVED, archivedPost.getStatus());
        assertFalse(archivedPost.isPublished());
    }
}