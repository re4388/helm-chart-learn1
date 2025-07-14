package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.FileMetadata;
import com.example.demoapp.domain.port.FileStorageService;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.UUID;

/**
 * Use case for uploading files
 * 用例：上傳檔案
 */
@Service
public class UploadFileUseCase {
    
    private final FileStorageService fileStorageService;
    
    public UploadFileUseCase(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * Execute use case to upload a file
     * 執行用例以上傳檔案
     */
    public FileMetadata execute(String bucketName, String originalFileName, 
                               InputStream inputStream, String contentType, 
                               long size, String uploadedBy) {
        
        validateUploadRequest(bucketName, originalFileName, inputStream, contentType, size);
        
        // Generate unique filename to avoid conflicts
        String uniqueFileName = generateUniqueFileName(originalFileName);
        
        return fileStorageService.uploadFile(
            bucketName.trim(), 
            uniqueFileName, 
            inputStream, 
            contentType.trim(), 
            size, 
            uploadedBy != null ? uploadedBy.trim() : "anonymous"
        );
    }
    
    private void validateUploadRequest(String bucketName, String originalFileName, 
                                     InputStream inputStream, String contentType, long size) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Original file name cannot be null or empty");
        }
        
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        
        if (size <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }
        
        // Check file size limit (e.g., 100MB)
        if (size > 100 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 100MB");
        }
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}