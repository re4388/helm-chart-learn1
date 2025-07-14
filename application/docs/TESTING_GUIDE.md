# Testing Guide

## File Upload Test Script

### Overview
A bash script is provided to easily test the MinIO file upload API. The script automatically creates random test files and uploads them to test the functionality.

### Location
```
scripts/test_file_upload.sh
```

### Usage

#### Basic Usage
```bash
# Test with default settings (bucket: test-bucket, uploader: test-user)
./scripts/test_file_upload.sh
```

#### Advanced Usage
```bash
# Test with custom bucket name
./scripts/test_file_upload.sh my-custom-bucket

# Test with custom bucket and uploader
./scripts/test_file_upload.sh my-custom-bucket john.doe
```

### What the Script Does

1. **Health Check**: Verifies the application is running on localhost:8080
2. **File Generation**: Creates a random test file with:
   - Random filename (test_file_[timestamp].txt)
   - Random content using predefined words
   - Timestamp for uniqueness
3. **Upload Test**: Uses curl to upload the file via POST /api/files/upload
4. **Response Display**: Shows the upload response in JSON format
5. **Helpful Commands**: Provides ready-to-use curl commands for:
   - Downloading the uploaded file
   - Getting file metadata
   - Checking file existence
   - Deleting the file
   - Listing bucket contents
6. **Cleanup**: Removes the temporary test file

### Example Output

```bash
$ ./scripts/test_file_upload.sh

=== MinIO File Upload Test Script ===

Checking if application is running...
✅ Application is running

Creating test file...
📄 Filename: test_file_1703123456789.txt
📝 Content: hello world test architecture (Generated at: Wed Dec 20 10:30:56 UTC 2023)
✅ Test file created

Uploading file to MinIO...
🚀 Uploading to bucket: test-bucket
👤 Uploader: test-user

✅ File uploaded successfully!

📋 Upload Response:
{
  "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.txt",
  "originalFileName": "test_file_1703123456789.txt",
  "contentType": "text/plain",
  "size": 65,
  "bucketName": "test-bucket",
  "uploadedAt": "2023-12-20T10:30:56",
  "uploadedBy": "test-user",
  "success": true,
  "message": "File uploaded successfully"
}

🔗 Useful commands for this file:
📥 Download: curl -o downloaded_test_file_1703123456789.txt 'http://localhost:8080/api/files/download/test-bucket/a1b2c3d4-e5f6-7890-abcd-ef1234567890.txt'
📋 Metadata: curl 'http://localhost:8080/api/files/metadata/test-bucket/a1b2c3d4-e5f6-7890-abcd-ef1234567890.txt'
❓ Exists: curl 'http://localhost:8080/api/files/exists/test-bucket/a1b2c3d4-e5f6-7890-abcd-ef1234567890.txt'
🗑️ Delete: curl -X DELETE 'http://localhost:8080/api/files/test-bucket/a1b2c3d4-e5f6-7890-abcd-ef1234567890.txt'
📂 List bucket: curl 'http://localhost:8080/api/files/list/test-bucket'

Cleaning up temporary file...
✅ Cleanup completed

=== Test completed ===

💡 Tips:
• Run with custom bucket: ./scripts/test_file_upload.sh my-bucket
• Run with custom uploader: ./scripts/test_file_upload.sh my-bucket john.doe
• Check MinIO console: http://localhost:9003 (minioadmin/minioadmin)
• View all files in bucket: curl 'http://localhost:8080/api/files/list/test-bucket'
```

### Prerequisites

1. **Application Running**: The Spring Boot application must be running on localhost:8080
2. **MinIO Running**: MinIO server must be running on localhost:9002
3. **Dependencies**: The script requires:
   - `curl` command
   - `jq` command (optional, for JSON formatting)
   - `bash` shell

### Error Handling

The script includes error handling for common scenarios:
- Application not running
- Upload failures
- Invalid responses

### Integration with CI/CD

The script can be easily integrated into CI/CD pipelines:

```bash
# In your CI/CD script
./scripts/test_file_upload.sh test-ci-bucket ci-user
if [ $? -eq 0 ]; then
    echo "File upload test passed"
else
    echo "File upload test failed"
    exit 1
fi
```

### Customization

You can modify the script to:
- Change the random word list
- Adjust file content format
- Add more test scenarios
- Include different file types
- Test with larger files

### Related Documentation

- [API Documentation](API_DOCUMENTATION.md) - Complete API reference
- [MinIO Setup Guide](MINIO_SETUP.md) - MinIO installation and configuration
- [Main README](../README.md) - Project overview and quick start