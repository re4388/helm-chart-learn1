package com.example.demoapp.adapter.web.controller;

import com.example.demoapp.adapter.web.dto.FileMetadataResponse;
import com.example.demoapp.adapter.web.dto.FileUploadResponse;
import com.example.demoapp.application.usecase.DownloadFileUseCase;
import com.example.demoapp.application.usecase.ManageFileUseCase;
import com.example.demoapp.application.usecase.UploadFileUseCase;
import com.example.demoapp.domain.model.FileData;
import com.example.demoapp.domain.model.FileMetadata;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for File operations following hexagonal architecture
 * 遵循六角架構的檔案操作 REST 控制器
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final ManageFileUseCase manageFileUseCase;

    public FileController(UploadFileUseCase uploadFileUseCase, 
                         DownloadFileUseCase downloadFileUseCase,
                         ManageFileUseCase manageFileUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.downloadFileUseCase = downloadFileUseCase;
        this.manageFileUseCase = manageFileUseCase;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucket", defaultValue = "default") String bucketName,
            @RequestParam(value = "uploadedBy", defaultValue = "anonymous") String uploadedBy) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(FileUploadResponse.failure("File is empty"));
            }

            FileMetadata metadata = uploadFileUseCase.execute(
                bucketName,
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getContentType(),
                file.getSize(),
                uploadedBy
            );

            return ResponseEntity.ok(FileUploadResponse.success(metadata));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.failure(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FileUploadResponse.failure("Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{bucketName}/{fileName}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String bucketName,
            @PathVariable String fileName) {
        
        try {
            Optional<FileData> fileDataOpt = downloadFileUseCase.execute(bucketName, fileName);
            
            if (fileDataOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileData fileData = fileDataOpt.get();
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileData.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + fileData.getOriginalFileName() + "\"")
                .contentLength(fileData.getSize())
                .body(new InputStreamResource(fileData.getInputStream()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to download file: " + e.getMessage());
        }
    }

    @GetMapping("/metadata/{bucketName}/{fileName}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(
            @PathVariable String bucketName,
            @PathVariable String fileName) {
        
        try {
            Optional<FileMetadata> metadataOpt = manageFileUseCase.getFileMetadata(bucketName, fileName);
            
            return metadataOpt
                .map(metadata -> ResponseEntity.ok(FileMetadataResponse.fromDomain(metadata)))
                .orElse(ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list/{bucketName}")
    public ResponseEntity<List<FileMetadataResponse>> listFiles(@PathVariable String bucketName) {
        try {
            List<FileMetadata> files = manageFileUseCase.listFiles(bucketName);
            List<FileMetadataResponse> responses = files.stream()
                .map(FileMetadataResponse::fromDomain)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{bucketName}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String bucketName,
            @PathVariable String fileName) {
        
        try {
            manageFileUseCase.deleteFile(bucketName, fileName);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/{bucketName}/{fileName}")
    public ResponseEntity<Boolean> fileExists(
            @PathVariable String bucketName,
            @PathVariable String fileName) {
        
        try {
            boolean exists = downloadFileUseCase.fileExists(bucketName, fileName);
            return ResponseEntity.ok(exists);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}