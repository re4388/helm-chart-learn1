package com.example.demoapp.domain.port;

import com.example.demoapp.domain.model.FileData;
import com.example.demoapp.domain.model.FileMetadata;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Domain service interface for file storage operations
 * 檔案儲存操作的領域服務介面
 */
public interface FileStorageService {
    
    /**
     * Upload a file to storage
     * 上傳檔案到儲存系統
     */
    FileMetadata uploadFile(String bucketName, String fileName, InputStream inputStream, 
                           String contentType, long size, String uploadedBy);
    
    /**
     * Download a file from storage
     * 從儲存系統下載檔案
     */
    Optional<FileData> downloadFile(String bucketName, String fileName);
    
    /**
     * Check if file exists
     * 檢查檔案是否存在
     */
    boolean fileExists(String bucketName, String fileName);
    
    /**
     * Delete a file from storage
     * 從儲存系統刪除檔案
     */
    void deleteFile(String bucketName, String fileName);
    
    /**
     * Get file metadata
     * 取得檔案元資料
     */
    Optional<FileMetadata> getFileMetadata(String bucketName, String fileName);
    
    /**
     * List all files in bucket
     * 列出儲存桶中的所有檔案
     */
    List<FileMetadata> listFiles(String bucketName);
}