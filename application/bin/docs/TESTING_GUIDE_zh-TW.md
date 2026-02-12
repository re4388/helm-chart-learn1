# 測試指南

## 檔案上傳測試腳本

### 概述
提供了一個 bash 腳本來輕鬆測試 MinIO 檔案上傳 API。該腳本會自動建立隨機測試檔案並上傳它們來測試功能。

### 位置
```
scripts/test_file_upload.sh
```

### 使用方法

#### 基本使用
```bash
# 使用預設設定測試（bucket: test-bucket, uploader: test-user）
./scripts/test_file_upload.sh
```

#### 進階使用
```bash
# 使用自訂 bucket 名稱測試
./scripts/test_file_upload.sh my-custom-bucket

# 使用自訂 bucket 和上傳者測試
./scripts/test_file_upload.sh my-custom-bucket john.doe
```

### 腳本功能

1. **健康檢查**：驗證應用程式是否在 localhost:8080 上執行
2. **檔案產生**：建立隨機測試檔案，包含：
   - 隨機檔名（test_file_[timestamp].txt）
   - 使用預定義單字的隨機內容
   - 時間戳記以確保唯一性
3. **上傳測試**：使用 curl 透過 POST /api/files/upload 上傳檔案
4. **回應顯示**：以 JSON 格式顯示上傳回應
5. **有用的指令**：提供可直接使用的 curl 指令用於：
   - 下載已上傳的檔案
   - 取得檔案元資料
   - 檢查檔案是否存在
   - 刪除檔案
   - 列出 bucket 內容
6. **清理**：移除暫存測試檔案

### 範例輸出

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

### 前置需求

1. **應用程式執行中**：Spring Boot 應用程式必須在 localhost:8080 上執行
2. **MinIO 執行中**：MinIO server 必須在 localhost:9002 上執行
3. **相依性**：腳本需要：
   - `curl` 指令
   - `jq` 指令（選用，用於 JSON 格式化）
   - `bash` shell

### 錯誤處理

腳本包含常見情況的錯誤處理：
- 應用程式未執行
- 上傳失敗
- 無效回應

### 與 CI/CD 整合

腳本可以輕鬆整合到 CI/CD pipeline 中：

```bash
# 在您的 CI/CD 腳本中
./scripts/test_file_upload.sh test-ci-bucket ci-user
if [ $? -eq 0 ]; then
    echo "File upload test passed"
else
    echo "File upload test failed"
    exit 1
fi
```

### 自訂化

您可以修改腳本來：
- 變更隨機單字清單
- 調整檔案內容格式
- 新增更多測試情境
- 包含不同的檔案類型
- 測試更大的檔案

### 相關文件

- [API Documentation](API_DOCUMENTATION.md) - 完整的 API 參考
- [MinIO Setup Guide](MINIO_SETUP.md) - MinIO 安裝和配置
- [Main README](../README.md) - 專案概述和快速開始

## 其他測試方法

### 手動測試

#### 基本檔案上傳測試
```bash
# 建立測試檔案
echo "這是測試內容" > test.txt

# 上傳檔案
curl -X POST \
  -F "file=@test.txt" \
  -F "bucket=test-bucket" \
  -F "uploadedBy=manual-tester" \
  http://localhost:8080/api/files/upload

# 清理
rm test.txt
```

#### 不同檔案類型測試
```bash
# 測試圖片檔案（建立假的圖片檔案）
echo "fake image content" > test.jpg
curl -X POST \
  -F "file=@test.jpg" \
  -F "bucket=images" \
  -F "uploadedBy=image-tester" \
  http://localhost:8080/api/files/upload

# 測試 PDF 檔案（建立假的 PDF 檔案）
echo "%PDF-1.4 fake pdf content" > test.pdf
curl -X POST \
  -F "file=@test.pdf" \
  -F "bucket=documents" \
  -F "uploadedBy=pdf-tester" \
  http://localhost:8080/api/files/upload
```

### 效能測試

#### 大檔案測試
```bash
# 建立 10MB 測試檔案
dd if=/dev/zero of=large_test.bin bs=1M count=10

# 上傳大檔案
curl -X POST \
  -F "file=@large_test.bin" \
  -F "bucket=performance-test" \
  -F "uploadedBy=perf-tester" \
  http://localhost:8080/api/files/upload

# 清理
rm large_test.bin
```

#### 並行上傳測試
```bash
# 建立多個檔案並並行上傳
for i in {1..5}; do
  echo "Test content $i" > "test_$i.txt"
  curl -X POST \
    -F "file=@test_$i.txt" \
    -F "bucket=concurrent-test" \
    -F "uploadedBy=concurrent-tester-$i" \
    http://localhost:8080/api/files/upload &
done

# 等待所有上傳完成
wait

# 清理
rm test_*.txt
```

### 錯誤情境測試

#### 測試檔案大小限制
```bash
# 建立超過限制的檔案（假設限制是 100MB，建立 101MB）
dd if=/dev/zero of=oversized.bin bs=1M count=101

# 嘗試上傳（應該失敗）
curl -X POST \
  -F "file=@oversized.bin" \
  -F "bucket=error-test" \
  -F "uploadedBy=error-tester" \
  http://localhost:8080/api/files/upload

# 清理
rm oversized.bin
```

#### 測試空檔案
```bash
# 建立空檔案
touch empty.txt

# 嘗試上傳空檔案（應該失敗）
curl -X POST \
  -F "file=@empty.txt" \
  -F "bucket=error-test" \
  -F "uploadedBy=error-tester" \
  http://localhost:8080/api/files/upload

# 清理
rm empty.txt
```

## 測試最佳實務

### 測試前檢查清單
1. ✅ 確認應用程式正在執行
2. ✅ 確認 MinIO 正在執行
3. ✅ 檢查網路連線
4. ✅ 驗證憑證設定

### 測試後清理
1. 🧹 刪除測試檔案
2. 🧹 清理測試 bucket（如果需要）
3. 🧹 檢查 MinIO 儲存使用量

### 監控和日誌
- 監控應用程式日誌中的錯誤
- 檢查 MinIO server 日誌
- 使用 MinIO console 監控儲存使用量

這個測試指南提供了全面的測試方法，從自動化腳本到手動測試，再到效能和錯誤情境測試，確保您的檔案上傳功能運作正常。