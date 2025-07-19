package com.example.demoapp.adapter.web.controller;

import com.example.demoapp.adapter.web.dto.FileMetadataResponse;
import com.example.demoapp.adapter.web.dto.FileUploadResponse;
import com.example.demoapp.application.usecase.DownloadFileUseCase;
import com.example.demoapp.application.usecase.ManageFileUseCase;
import com.example.demoapp.application.usecase.UploadFileUseCase;
import com.example.demoapp.domain.model.FileData;
import com.example.demoapp.domain.model.FileMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "檔案管理", description = "提供檔案上傳、下載、元資料查詢及刪除等操作")
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

    @Operation(summary = "上傳檔案", description = "將檔案上傳到指定的儲存桶。支援指定儲存桶名稱和上傳者。")
    @ApiResponse(responseCode = "200", description = "檔案上傳成功",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class)))
    @ApiResponse(responseCode = "400", description = "無效的請求或檔案為空",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class)))
    @ApiResponse(responseCode = "500", description = "伺服器內部錯誤",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class)))
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "要上傳的檔案", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "儲存桶名稱，預設為 'default'", example = "my-bucket") @RequestParam(value = "bucket", defaultValue = "default") String bucketName,
            @Parameter(description = "上傳者，預設為 'anonymous'", example = "user123") @RequestParam(value = "uploadedBy", defaultValue = "anonymous") String uploadedBy) {
        
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

    @Operation(summary = "下載檔案", description = "從指定的儲存桶下載檔案。")
    @ApiResponse(responseCode = "200", description = "檔案下載成功",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
    @ApiResponse(responseCode = "400", description = "無效的請求")
    @ApiResponse(responseCode = "404", description = "檔案未找到")
    @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    @GetMapping("/download/{bucketName}/{fileName}")
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "儲存桶名稱", required = true, example = "my-bucket") @PathVariable String bucketName,
            @Parameter(description = "檔案名稱", required = true, example = "a1b2c3d4e5f6g7h8i9j0.txt") @PathVariable String fileName) {
        
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

    @Operation(summary = "獲取檔案元資料", description = "獲取指定檔案的元資料資訊。")
    @ApiResponse(responseCode = "200", description = "成功獲取檔案元資料",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileMetadataResponse.class)))
    @ApiResponse(responseCode = "400", description = "無效的請求")
    @ApiResponse(responseCode = "404", description = "檔案元資料未找到")
    @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    @GetMapping("/metadata/{bucketName}/{fileName}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(
            @Parameter(description = "儲存桶名稱", required = true, example = "my-bucket") @PathVariable String bucketName,
            @Parameter(description = "檔案名稱", required = true, example = "a1b2c3d4e5f6g7h8i9j0.txt") @PathVariable String fileName) {
        
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

    @Operation(summary = "列出儲存桶中的檔案", description = "列出指定儲存桶中的所有檔案元資料。")
    @ApiResponse(responseCode = "200", description = "成功列出檔案",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileMetadataResponse.class)))
    @ApiResponse(responseCode = "400", description = "無效的請求")
    @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    @GetMapping("/list/{bucketName}")
    public ResponseEntity<List<FileMetadataResponse>> listFiles(@Parameter(description = "儲存桶名稱", required = true, example = "my-bucket") @PathVariable String bucketName) {
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

    @Operation(summary = "刪除檔案", description = "從指定的儲存桶中刪除檔案。")
    @ApiResponse(responseCode = "204", description = "檔案成功刪除")
    @ApiResponse(responseCode = "404", description = "檔案未找到")
    @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    @DeleteMapping("/{bucketName}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "儲存桶名稱", required = true, example = "my-bucket") @PathVariable String bucketName,
            @Parameter(description = "檔案名稱", required = true, example = "a1b2c3d4e5f6g7h8i9j0.txt") @PathVariable String fileName) {
        
        try {
            manageFileUseCase.deleteFile(bucketName, fileName);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "檢查檔案是否存在", description = "檢查指定儲存桶中是否存在某個檔案。")
    @ApiResponse(responseCode = "200", description = "成功檢查檔案是否存在",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "400", description = "無效的請求")
    @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    @GetMapping("/exists/{bucketName}/{fileName}")
    public ResponseEntity<Boolean> fileExists(
            @Parameter(description = "儲存桶名稱", required = true, example = "my-bucket") @PathVariable String bucketName,
            @Parameter(description = "檔案名稱", required = true, example = "a1b2c3d4e5f6g7h8i9j0.txt") @PathVariable String fileName) {
        
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