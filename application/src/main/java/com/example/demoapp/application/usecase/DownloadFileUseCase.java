package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.FileData;
import com.example.demoapp.domain.port.FileStorageService;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Use case for downloading files
 * 用例：下載檔案
 */
@Service
public class DownloadFileUseCase {
    
    private final FileStorageService fileStorageService;
    
    public DownloadFileUseCase(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * Execute use case to download a file
     * 執行用例以下載檔案
     */
    public Optional<FileData> execute(String bucketName, String fileName) {
        validateDownloadRequest(bucketName, fileName);
        
        return fileStorageService.downloadFile(bucketName.trim(), fileName.trim());
    }
    
    /**
     * Execute use case to check if file exists
     * 執行用例以檢查檔案是否存在
     */
    public boolean fileExists(String bucketName, String fileName) {
        validateDownloadRequest(bucketName, fileName);
        
        return fileStorageService.fileExists(bucketName.trim(), fileName.trim());
    }
    
    private void validateDownloadRequest(String bucketName, String fileName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
    }
}