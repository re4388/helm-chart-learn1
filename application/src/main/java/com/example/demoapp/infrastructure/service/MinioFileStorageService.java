package com.example.demoapp.infrastructure.service;

import com.example.demoapp.domain.model.FileData;
import com.example.demoapp.domain.model.FileMetadata;
import com.example.demoapp.domain.port.FileStorageService;
import com.example.demoapp.infrastructure.observability.MetricsService;
import com.example.demoapp.infrastructure.observability.TracingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.minio.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MinIO implementation of FileStorageService
 * FileStorageService 的 MinIO 實作
 */
@Service
public class MinioFileStorageService implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MinioFileStorageService.class);
    
    private final MinioClient minioClient;
    private final MetricsService metricsService;
    private final TracingService tracingService;
    private final Counter minioUploadsTotal;
    private final Counter minioDownloadsTotal;
    
    public MinioFileStorageService(MinioClient minioClient, 
                                  MetricsService metricsService,
                                  TracingService tracingService,
                                  MeterRegistry meterRegistry) {
        this.minioClient = minioClient;
        this.metricsService = metricsService;
        this.tracingService = tracingService;
        this.minioUploadsTotal = Counter.builder("minio_operations_total")
            .tag("operation", "upload")
            .description("Total number of MinIO file uploads")
            .register(meterRegistry);
        this.minioDownloadsTotal = Counter.builder("minio_operations_total")
            .tag("operation", "download")
            .description("Total number of MinIO file downloads")
            .register(meterRegistry);
    }
    
    @Override
    public FileMetadata uploadFile(String bucketName, String fileName, InputStream inputStream, 
                                  String contentType, long size, String uploadedBy) {
        return tracingService.traceExternalCall("minio", "uploadFile", () -> {
            Timer.Sample sample = metricsService.startMinioTimer("upload");
            
            try {
                logger.info("Starting file upload: bucket={}, fileName={}, size={}, uploadedBy={}", 
                           bucketName, fileName, size, uploadedBy);
                
                tracingService.addSpanAttribute("minio.bucket", bucketName);
                tracingService.addSpanAttribute("minio.fileName", fileName);
                tracingService.addSpanAttribute("minio.contentType", contentType);
                tracingService.addSpanAttribute("minio.size", String.valueOf(size));
                
                // Ensure bucket exists
                ensureBucketExists(bucketName);
                
                // Upload file
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
                );
                
                FileMetadata metadata = FileMetadata.create(fileName, fileName, contentType, size, bucketName, uploadedBy);
                
                metricsService.stopMinioTimer(sample, "upload", "success");
                metricsService.recordFileOperation("upload", "success", size);
                minioUploadsTotal.increment();
                
                logger.info("Successfully uploaded file: bucket={}, fileName={}, size={}", 
                           bucketName, fileName, size);
                
                return metadata;
                
            } catch (Exception e) {
                metricsService.stopMinioTimer(sample, "upload", "error");
                metricsService.recordFileOperation("upload", "error", size);
                
                logger.error("Failed to upload file: bucket={}, fileName={}, error={}", 
                            bucketName, fileName, e.getMessage(), e);
                
                throw new RuntimeException("Failed to upload file: " + fileName, e);
            }
        });
    }
    
    @Override
    public Optional<FileData> downloadFile(String bucketName, String fileName) {
        try {
            // Check if file exists
            if (!fileExists(bucketName, fileName)) {
                return Optional.empty();
            }
            
            // Get file metadata
            Optional<FileMetadata> metadataOpt = getFileMetadata(bucketName, fileName);
            if (metadataOpt.isEmpty()) {
                return Optional.empty();
            }
            
            // Get file stream
            InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );
            minioDownloadsTotal.increment();
            return Optional.of(new FileData(metadataOpt.get(), inputStream));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + fileName, e);
        }
    }
    
    @Override
    public boolean fileExists(String bucketName, String fileName) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }
    
    @Override
    public Optional<FileMetadata> getFileMetadata(String bucketName, String fileName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );
            
            LocalDateTime uploadedAt = stat.lastModified().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
            
            return Optional.of(new FileMetadata(
                fileName,
                fileName,
                stat.contentType(),
                stat.size(),
                bucketName,
                uploadedAt,
                "unknown" // MinIO doesn't store uploader info by default
            ));
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<FileMetadata> listFiles(String bucketName) {
        try {
            List<FileMetadata> files = new ArrayList<>();
            
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                LocalDateTime uploadedAt = item.lastModified().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
                
                files.add(new FileMetadata(
                    item.objectName(),
                    item.objectName(),
                    "application/octet-stream", // Default content type
                    item.size(),
                    bucketName,
                    uploadedAt,
                    "unknown"
                ));
            }
            
            return files;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files in bucket: " + bucketName, e);
        }
    }
    
    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure bucket exists: " + bucketName, e);
        }
    }
}