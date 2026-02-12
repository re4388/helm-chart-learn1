package com.example.demoapp.domain.dto;

import com.example.demoapp.domain.model.FileMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response DTO for file metadata endpoints
 * 檔案元資料端點的回應 DTO
 */
@Schema(description = "檔案元資料回應物件")
public class FileMetadataResponse {
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
    @Schema(description = "上傳者", example = "user123")
    private final String uploadedBy;
    @Schema(description = "檔案副檔名", example = "txt")
    private final String fileExtension;
    @Schema(description = "是否為圖片", example = "true")
    private final boolean isImage;
    @Schema(description = "是否為文件", example = "false")
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