# 六角架構實作

本專案已重構以遵循 **六角架構**（也稱為 **Ports and Adapters** 或 **Clean Architecture**）原則。

## 架構概覽

```
┌─────────────────────────────────────────────────────────────┐
│                    Adapters (適配器層)                        │
├─────────────────────────────────────────────────────────────┤
│  Web Controllers  │  DTOs  │  MongoDB Repositories          │
│  REST APIs        │        │  MinIO File Storage            │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                Application Layer (應用層)                     │
├─────────────────────────────────────────────────────────────┤
│  Use Cases (用例)                                            │
│  - GetPostsUseCase, ManagePostUseCase                       │
│  - UploadFileUseCase, DownloadFileUseCase                   │
│  - GetGreetingUseCase, GetHealthStatusUseCase               │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Domain Layer (領域層)                        │
├─────────────────────────────────────────────────────────────┤
│  Entities (實體):                                            │
│  - Post, Greeting, HealthStatus                            │
│  - FileMetadata, FileData                                  │
│                                                             │
│  Ports (埠):                                                │
│  - PostService, PostRepository                             │
│  - FileStorageService                                       │
│  - GreetingService, HealthService                          │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│               Infrastructure Layer (基礎設施層)                │
├─────────────────────────────────────────────────────────────┤
│  Service Implementations:                                   │
│  - PostServiceImpl, GreetingServiceImpl                    │
│  - MinioFileStorageService                                  │
│  - HealthServiceImpl                                        │
│                                                             │
│  Repository Implementations:                               │
│  - PostRepositoryImpl (MongoDB)                            │
└─────────────────────────────────────────────────────────────┘
```

## 核心原則

### 1. 依賴反轉
- **領域層** 定義介面（ports）
- **基礎設施層** 實作這些介面
- 相依性指向領域層

### 2. 關注點分離
- **領域層**：業務邏輯和規則
- **應用層**：用例和編排
- **基礎設施層**：技術實作
- **適配器層**：外部介面

### 3. 可測試性
- 領域邏輯被隔離且容易測試
- 用例可以使用模擬相依性進行測試
- 基礎設施可以替換以進行測試

## 專案結構

```
src/main/java/com/example/demoapp/
├── domain/                          # 領域層
│   ├── model/                       # 領域實體
│   │   ├── Post.java
│   │   ├── Greeting.java
│   │   ├── HealthStatus.java
│   │   ├── FileMetadata.java
│   │   └── FileData.java
│   └── port/                        # 領域埠 (介面)
│       ├── PostService.java
│       ├── PostRepository.java
│       ├── FileStorageService.java
│       ├── GreetingService.java
│       └── HealthService.java
├── application/                     # 應用層
│   └── usecase/                     # 用例
│       ├── GetPostsUseCase.java
│       ├── ManagePostUseCase.java
│       ├── UploadFileUseCase.java
│       ├── DownloadFileUseCase.java
│       ├── ManageFileUseCase.java
│       ├── GetGreetingUseCase.java
│       └── GetHealthStatusUseCase.java
├── infrastructure/                  # 基礎設施層
│   ├── service/                     # 服務實作
│   │   ├── PostServiceImpl.java
│   │   ├── MinioFileStorageService.java
│   │   ├── GreetingServiceImpl.java
│   │   └── HealthServiceImpl.java
│   ├── config/                      # 配置
│   │   └── MinioConfig.java
│   └── persistence/                 # 持久化實作
│       └── mongodb/
│           ├── PostDocument.java
│           ├── PostMongoRepository.java
│           └── PostRepositoryImpl.java
└── adapter/                         # 適配器層
    └── web/                         # Web 適配器
        ├── controller/              # REST 控制器
        │   ├── HelloController.java
        │   ├── PostController.java
        │   └── FileController.java
        └── dto/                     # 資料傳輸物件
            ├── GreetingResponse.java
            ├── HealthResponse.java
            ├── PostResponse.java
            ├── CreatePostRequest.java
            ├── UpdatePostRequest.java
            ├── FileUploadResponse.java
            ├── FileMetadataResponse.java
            ├── CreatePostRequest.java
            └── UpdatePostRequest.java
```

## API 端點

### 問候 APIs
- `GET /` - 取得一般問候訊息
- `GET /hello/{name}` - 取得個人化問候訊息
- `GET /health` - 取得健康狀態

### 文章 APIs
- `GET /api/posts` - 取得所有文章
- `GET /api/posts/published` - 僅取得已發布的文章
- `GET /api/posts/{id}` - 根據 ID 取得文章
- `POST /api/posts` - 建立新文章
- `PUT /api/posts/{id}` - 更新文章
- `PUT /api/posts/{id}/publish` - 發布文章
- `PUT /api/posts/{id}/archive` - 封存文章
- `DELETE /api/posts/{id}` - 刪除文章

### 檔案管理 APIs
- `POST /api/files/upload` - 上傳檔案到 MinIO
- `GET /api/files/download/{bucket}/{filename}` - 從 MinIO 下載檔案
- `GET /api/files/metadata/{bucket}/{filename}` - 取得檔案元資料
- `GET /api/files/list/{bucket}` - 列出儲存桶中的檔案
- `GET /api/files/exists/{bucket}/{filename}` - 檢查檔案是否存在
- `DELETE /api/files/{bucket}/{filename}` - 刪除檔案

## MongoDB 整合

應用程式連接到 `localhost:27017` 上的 MongoDB 並使用 `demo` 資料庫。現有的文章資料結構得以保留：

```json
{
  "_id": "post1",
  "createdAt": {
    "$date": "2023-01-01T10:00:00Z"
  },
  "status": "PUBLISHED",
  "title": "My First Post",
  "content": "This is the content of my first post."
}
```

## 此架構的優點

1. **可維護性**：清楚的關注點分離
2. **可測試性**：容易對業務邏輯進行單元測試
3. **靈活性**：容易替換實作
4. **可擴展性**：容易新增功能
5. **獨立性**：領域邏輯獨立於框架

## 執行應用程式

### 前置需求
1. **MongoDB**: 確保 MongoDB 在 localhost:27017 上執行
2. **MinIO**: 在 port 9002 上啟動 MinIO 伺服器 (參見 [MinIO 設定指南](docs/MINIO_SETUP.md))

### 啟動 MinIO (檔案操作必需)
```bash
# 使用 Docker (建議)
docker run -d \
  --name minio-server \
  -p 9002:9000 \
  -p 9003:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

### 啟動應用程式
```bash
mvn spring-boot:run
```

### 存取點
- **應用程式**: http://localhost:8080
- **MinIO 控制台**: http://localhost:9003 (minioadmin/minioadmin)
- **MongoDB**: localhost:27017

### API 呼叫範例：

```bash
# 取得所有文章
curl http://localhost:8080/api/posts

# 取得特定文章
curl http://localhost:8080/api/posts/post1

# 建立新文章
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"post2","title":"New Post","content":"New content"}'

# 發布文章
curl -X PUT http://localhost:8080/api/posts/post2/publish

# 上傳檔案
curl -X POST \
  -F "file=@document.pdf" \
  -F "bucket=documents" \
  -F "uploadedBy=john.doe" \
  http://localhost:8080/api/files/upload

# 下載檔案 (將 {filename} 替換為上傳回應中的實際檔名)
curl -o downloaded-file.pdf \
  http://localhost:8080/api/files/download/documents/{filename}
```

## 測試工具

### 自動化檔案上傳測試
提供便利的測試腳本來測試檔案上傳功能：

```bash
# 使用預設設定執行測試
./scripts/test_file_upload.sh

# 使用自訂儲存桶
./scripts/test_file_upload.sh my-bucket

# 使用自訂儲存桶和上傳者
./scripts/test_file_upload.sh my-bucket john.doe
```

測試腳本會自動：
1. ✅ 檢查應用程式是否正在執行
2. ✅ 產生隨機測試檔案和內容
3. ✅ 使用 API 上傳檔案到 MinIO
4. ✅ 顯示回應和有用的後續指令
5. ✅ 清理暫存檔案