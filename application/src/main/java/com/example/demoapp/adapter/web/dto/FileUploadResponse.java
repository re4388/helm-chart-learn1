package com.example.demoapp.adapter.web.dto;

import com.example.demoapp.domain.model.FileMetadata;
import java.time.LocalDateTime;

/**
 * Response DTO for file upload endpoints
 * 檔案上傳端點的回應 DTO
 */
public class FileUploadResponse {
    private final String fileName;
    private final String originalFileName;
    private final String contentType;
    private final long size;
    private final String bucketName;
    private final LocalDateTime uploadedAt;
    private final String uploadedBy;
    private final boolean success;
    private final String message;

    public FileUploadResponse(String fileName, String originalFileName, String contentType, 
                             long size, String bucketName, LocalDateTime uploadedAt, 
                             String uploadedBy, boolean success, String message) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.size = size;
        this.bucketName = bucketName;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
        this.success = success;
        this.message = message;
    }

    public static FileUploadResponse success(FileMetadata metadata) {
        return new FileUploadResponse(
            metadata.getFileName(),
            metadata.getOriginalFileName(),
            metadata.getContentType(),
            metadata.getSize(),
            metadata.getBucketName(),
            metadata.getUploadedAt(),
            metadata.getUploadedBy(),
            true,
            "File uploaded successfully"
        );
    }

    public static FileUploadResponse failure(String message) {
        return new FileUploadResponse(
            null, null, null, 0, null, null, null, false, message
        );
    }

    // Getters
    public String getFileName() { return fileName; }
    public String getOriginalFileName() { return originalFileName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public String getBucketName() { return bucketName; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}