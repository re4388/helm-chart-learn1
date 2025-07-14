package com.example.demoapp.domain.model;

import java.io.InputStream;

/**
 * Domain entity representing file data for download
 * 領域實體：下載的檔案資料
 */
public class FileData {
    private final FileMetadata metadata;
    private final InputStream inputStream;

    public FileData(FileMetadata metadata, InputStream inputStream) {
        this.metadata = metadata;
        this.inputStream = inputStream;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return metadata.getFileName();
    }

    public String getOriginalFileName() {
        return metadata.getOriginalFileName();
    }

    public String getContentType() {
        return metadata.getContentType();
    }

    public long getSize() {
        return metadata.getSize();
    }
}