package com.example.demoapp.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FileMetadataTest {

    @Test
    void shouldCreateFileMetadata() {
        // Given
        String fileName = "test-file.jpg";
        String originalFileName = "original-image.jpg";
        String contentType = "image/jpeg";
        long size = 1024L;
        String bucketName = "test-bucket";
        String uploadedBy = "testuser";
        
        // When
        FileMetadata metadata = FileMetadata.create(fileName, originalFileName, contentType, size, bucketName, uploadedBy);
        
        // Then
        assertEquals(fileName, metadata.getFileName());
        assertEquals(originalFileName, metadata.getOriginalFileName());
        assertEquals(contentType, metadata.getContentType());
        assertEquals(size, metadata.getSize());
        assertEquals(bucketName, metadata.getBucketName());
        assertEquals(uploadedBy, metadata.getUploadedBy());
        assertNotNull(metadata.getUploadedAt());
    }

    @Test
    void shouldDetectImageFile() {
        // Given
        FileMetadata metadata = FileMetadata.create("test.jpg", "test.jpg", "image/jpeg", 1024L, "bucket", "user");
        
        // When & Then
        assertTrue(metadata.isImage());
        assertFalse(metadata.isDocument());
    }

    @Test
    void shouldDetectDocumentFile() {
        // Given
        FileMetadata metadata = FileMetadata.create("test.pdf", "test.pdf", "application/pdf", 1024L, "bucket", "user");
        
        // When & Then
        assertFalse(metadata.isImage());
        assertTrue(metadata.isDocument());
    }

    @Test
    void shouldGetFileExtension() {
        // Given
        FileMetadata metadata = FileMetadata.create("test.jpg", "original.jpg", "image/jpeg", 1024L, "bucket", "user");
        
        // When & Then
        assertEquals(".jpg", metadata.getFileExtension());
    }
}