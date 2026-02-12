package com.example.demoapp.domain.dto;

import com.example.demoapp.domain.model.FileMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response DTO for file upload endpoints
 * 檔案上傳端點的回應 DTO
 */
@Schema(description = "檔案上傳回應物件")
public class FileUploadResponse {
    @Schema(description = "檔案名稱 (系統生成)", example = "a1b2c3d4e5f6g7h8i9j0.txt")
    private final String fileName;
    @Schema(description = "原始檔案名稱", example = "my_document.txt")
    private final String originalFileName;
    @Schema(description = "檔案內容類型 (MIME Type)", example = "text/plain")
    private final String contentType;
    @Schema(description = "檔案大小 (位元組)", example = "1024")
    private final long size;
    @Schema(description = "儲存檔案的儲存桶名稱", example = "my-bucket")
    private final String bucketName;
    @Schema(description = "檔案上傳時間", example = "2023-10-26T10:00:00")
    private final LocalDateTime uploadedAt;
    @Schema(description = "上傳者", example = "anonymous")
    private final String uploadedBy;
    @Schema(description = "上傳是否成功", example = "true")
    private final boolean success;
    @Schema(description = "上傳結果訊息", example = "File uploaded successfully")
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