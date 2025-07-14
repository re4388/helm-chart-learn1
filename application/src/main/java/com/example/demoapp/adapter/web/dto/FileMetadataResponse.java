package com.example.demoapp.adapter.web.dto;

import com.example.demoapp.domain.model.FileMetadata;
import java.time.LocalDateTime;

/**
 * Response DTO for file metadata endpoints
 * 檔案元資料端點的回應 DTO
 */
public class FileMetadataResponse {
    private final String fileName;
    private final String originalFileName;
    private final String contentType;
    private final long size;
    private final String bucketName;
    private final LocalDateTime uploadedAt;
    private final String uploadedBy;
    private final String fileExtension;
    private final boolean isImage;
    private final boolean isDocument;

    public FileMetadataResponse(String fileName, String originalFileName, String contentType, 
                               long size, String bucketName, LocalDateTime uploadedAt, 
                               String uploadedBy, String fileExtension, boolean isImage, boolean isDocument) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.size = size;
        this.bucketName = bucketName;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
        this.fileExtension = fileExtension;
        this.isImage = isImage;
        this.isDocument = isDocument;
    }

    public static FileMetadataResponse fromDomain(FileMetadata metadata) {
        return new FileMetadataResponse(
            metadata.getFileName(),
            metadata.getOriginalFileName(),
            metadata.getContentType(),
            metadata.getSize(),
            metadata.getBucketName(),
            metadata.getUploadedAt(),
            metadata.getUploadedBy(),
            metadata.getFileExtension(),
            metadata.isImage(),
            metadata.isDocument()
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
    public String getFileExtension() { return fileExtension; }
    public boolean isImage() { return isImage; }
    public boolean isDocument() { return isDocument; }
}