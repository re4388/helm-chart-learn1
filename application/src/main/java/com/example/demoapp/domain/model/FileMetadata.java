package com.example.demoapp.domain.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing file metadata
 * 領域實體：檔案元資料
 */
public class FileMetadata {
    private final String fileName;
    private final String originalFileName;
    private final String contentType;
    private final long size;
    private final String bucketName;
    private final LocalDateTime uploadedAt;
    private final String uploadedBy;

    public FileMetadata(String fileName, String originalFileName, String contentType, 
                       long size, String bucketName, LocalDateTime uploadedAt, String uploadedBy) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.size = size;
        this.bucketName = bucketName;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    public static FileMetadata create(String fileName, String originalFileName, 
                                    String contentType, long size, String bucketName, String uploadedBy) {
        return new FileMetadata(fileName, originalFileName, contentType, size, 
                              bucketName, LocalDateTime.now(), uploadedBy);
    }

    public String getFileName() {
        return fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getBucketName() {
        return bucketName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getFileExtension() {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return "";
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isDocument() {
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.startsWith("application/vnd.openxmlformats") ||
            contentType.startsWith("application/msword")
        );
    }
}