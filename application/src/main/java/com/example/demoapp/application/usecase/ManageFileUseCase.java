package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.FileMetadata;
import com.example.demoapp.domain.port.FileStorageService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Use case for managing files (list, delete, get metadata)
 * 用例：管理檔案（列表、刪除、取得元資料）
 */
@Service
public class ManageFileUseCase {
    
    private final FileStorageService fileStorageService;
    
    public ManageFileUseCase(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * Execute use case to list all files in bucket
     * 執行用例以列出儲存桶中的所有檔案
     */
    public List<FileMetadata> listFiles(String bucketName) {
        validateBucketName(bucketName);
        return fileStorageService.listFiles(bucketName.trim());
    }
    
    /**
     * Execute use case to get file metadata
     * 執行用例以取得檔案元資料
     */
    public Optional<FileMetadata> getFileMetadata(String bucketName, String fileName) {
        validateFileRequest(bucketName, fileName);
        return fileStorageService.getFileMetadata(bucketName.trim(), fileName.trim());
    }
    
    /**
     * Execute use case to delete a file
     * 執行用例以刪除檔案
     */
    public void deleteFile(String bucketName, String fileName) {
        validateFileRequest(bucketName, fileName);
        
        if (!fileStorageService.fileExists(bucketName.trim(), fileName.trim())) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        
        fileStorageService.deleteFile(bucketName.trim(), fileName.trim());
    }
    
    private void validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
    }
    
    private void validateFileRequest(String bucketName, String fileName) {
        validateBucketName(bucketName);
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
    }
}