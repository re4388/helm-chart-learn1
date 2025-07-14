# Demo Spring Boot 應用程式

這是一個簡單的 Spring Boot 應用程式，用於演示 Helm Chart 部署。

## 功能

### 基本功能
- **GET /** - 返回歡迎訊息和應用程式資訊
- **GET /hello/{name}** - 個人化問候訊息
- **GET /health** - 健康檢查端點
- **GET /actuator/health** - Spring Boot Actuator 健康檢查

### 文章管理 (MongoDB)
- **GET /api/posts** - 取得所有文章
- **POST /api/posts** - 建立新文章
- **PUT /api/posts/{id}/publish** - 發布文章

### 檔案管理 (MinIO)
- **POST /api/files/upload** - 上傳檔案到 MinIO
- **GET /api/files/download/{bucket}/{filename}** - 從 MinIO 下載檔案
- **GET /api/files/list/{bucket}** - 列出儲存桶中的檔案

## 架構說明

本應用程式採用 **六角架構 (Hexagonal Architecture)** 設計，具備完整的檔案管理和文章管理功能。

詳細架構說明請參閱：[六角架構實作文件](README_HEXAGONAL_ARCHITECTURE.md)

## 前置需求

1. **Java 17+**
2. **Maven 3.6+**
3. **MongoDB** (port 27017)
4. **MinIO** (port 9002) - 檔案儲存服務

## 快速啟動

### 1. 啟動 MinIO
```bash
docker run -d \
  --name minio-server \
  -p 9002:9000 \
  -p 9003:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

### 2. 啟動應用程式
```bash
# 編譯應用程式
mvn clean package

# 執行應用程式
mvn spring-boot:run

# 或者執行 JAR 檔案
java -jar target/demo-app-1.0.0.jar
```

## Docker 建置

```bash
# 先編譯應用程式
mvn clean package

# 建置 Docker 映像檔
docker build -t demo-app:1.0.0 .

# 執行容器
docker run -p 8080:8080 demo-app:1.0.0
```

## 測試端點

### 基本功能測試
```bash
curl http://localhost:8080/
curl http://localhost:8080/hello/World
curl http://localhost:8080/health
curl http://localhost:8080/actuator/health
```

### 文章管理測試
```bash
# 取得所有文章
curl http://localhost:8080/api/posts

# 建立新文章
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"post2","title":"測試文章","content":"這是測試內容"}'
```

### 檔案管理測試

#### 使用測試腳本 (推薦)
```bash
# 自動化測試檔案上傳
./scripts/test_file_upload.sh

# 使用自訂儲存桶
./scripts/test_file_upload.sh my-bucket

# 使用自訂儲存桶和上傳者
./scripts/test_file_upload.sh my-bucket john.doe
```

#### 手動測試
```bash
# 上傳檔案
curl -X POST \
  -F "file=@test.txt" \
  -F "bucket=test-bucket" \
  -F "uploadedBy=testuser" \
  http://localhost:8080/api/files/upload

# 列出檔案
curl http://localhost:8080/api/files/list/test-bucket
```

## 存取點

- **應用程式**: http://localhost:8080
- **MinIO 控制台**: http://localhost:9003 (minioadmin/minioadmin)

## 文件

### 英文文件
- **API 文件**: [API Documentation](docs/API_DOCUMENTATION.md)
- **MinIO 設定**: [MinIO Setup Guide](docs/MINIO_SETUP.md)
- **測試指南**: [Testing Guide](docs/TESTING_GUIDE.md)
- **架構說明**: [Hexagonal Architecture](README_HEXAGONAL_ARCHITECTURE.md)

### 繁體中文文件
- **MinIO 設定**: [MinIO 設定指南](docs/MINIO_SETUP_zh-TW.md)
- **測試指南**: [測試指南](docs/TESTING_GUIDE_zh-TW.md)