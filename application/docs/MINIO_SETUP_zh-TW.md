# MinIO 檔案儲存設定指南

## 概述

本應用程式使用 MinIO 作為物件儲存解決方案，用於檔案上傳和下載操作。MinIO 是一個高效能、S3 相容的物件儲存系統，非常適合儲存非結構化資料，如照片、影片、日誌檔案、備份和容器映像檔。

## MinIO 配置

### 預設設定
- **MinIO Server Port**: `9002`
- **MinIO Console Port**: `9003`
- **Access Key**: `minioadmin`
- **Secret Key**: `minioadmin`
- **預設 Bucket**: 根據 API 請求自動建立

### 應用程式配置

MinIO 設定在 `src/main/resources/application.yml` 中配置：

```yaml
minio:
  endpoint: http://localhost:9002
  access-key: minioadmin
  secret-key: minioadmin

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

## 安裝和設定

### 選項 1: Docker（推薦）

```bash
# 使用自訂 port 啟動 MinIO server
docker run -d \
  --name minio-server \
  -p 9002:9000 \
  -p 9003:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v minio-data:/data \
  minio/minio server /data --console-address ":9001"
```

### 選項 2: Docker Compose

建立 `docker-compose.yml` 檔案：

```yaml
version: '3.8'
services:
  minio:
    image: minio/minio
    container_name: minio-server
    ports:
      - "9002:9000"  # MinIO API
      - "9003:9001"  # MinIO Console
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

volumes:
  minio-data:
```

執行：
```bash
docker-compose up -d
```

### 選項 3: 本地安裝

1. 從 [https://min.io/download](https://min.io/download) 下載 MinIO
2. 設定為可執行檔並執行：

```bash
# Linux/macOS
chmod +x minio
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin
./minio server /data --address ":9002" --console-address ":9003"

# Windows
set MINIO_ROOT_USER=minioadmin
set MINIO_ROOT_PASSWORD=minioadmin
minio.exe server C:\data --address ":9002" --console-address ":9003"
```

## 驗證

### 1. 檢查 MinIO 健康狀態
```bash
curl http://localhost:9002/minio/health/live
```

### 2. 存取 MinIO Console
開啟瀏覽器並前往：[http://localhost:9003](http://localhost:9003)
- 使用者名稱：`minioadmin`
- 密碼：`minioadmin`

### 3. 測試 API 連線
```bash
# 測試 MinIO 是否可存取
curl -I http://localhost:9002
```

## Bucket 管理

### 自動 Bucket 建立
應用程式會在檔案上傳時自動建立 bucket。不需要手動建立 bucket。

### 手動 Bucket 建立（選用）
您可以透過以下方式手動建立 bucket：

1. **MinIO Console**：使用 http://localhost:9003 的網頁介面
2. **MinIO Client (mc)**：
```bash
# 安裝 mc client
curl https://dl.min.io/client/mc/release/linux-amd64/mc -o mc
chmod +x mc

# 配置 mc
./mc alias set local http://localhost:9002 minioadmin minioadmin

# 建立 bucket
./mc mb local/my-bucket
```

## 安全性考量

### 開發環境
- 預設憑證適用於開發環境
- MinIO 執行時不使用 TLS（僅 HTTP）

### 正式環境
1. **變更預設憑證**：
```yaml
minio:
  access-key: your-production-access-key
  secret-key: your-production-secret-key-min-8-chars
```

2. **啟用 TLS**：
```yaml
minio:
  endpoint: https://your-minio-server.com
```

3. **網路安全**：
- 使用防火牆規則限制存取
- 考慮在反向代理後執行 MinIO
- 在雲端環境中使用 VPC/私有網路

## 疑難排解

### 常見問題

1. **Port 已被使用**：
```bash
# 檢查什麼在使用 port 9002
lsof -i :9002
# 終止程序或使用不同的 port
```

2. **權限被拒絕**：
```bash
# 確保資料目錄可寫入
chmod 755 /data
```

3. **連線被拒絕**：
- 驗證 MinIO 是否正在執行：`docker ps` 或 `ps aux | grep minio`
- 檢查防火牆設定
- 驗證 port 配置

### 日誌和除錯

```bash
# Docker 日誌
docker logs minio-server

# 檢查 MinIO server 狀態
curl http://localhost:9002/minio/health/ready
```

## 效能調整

### 高流量應用程式

1. **增加檔案大小限制**：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 1GB
      max-request-size: 1GB
```

2. **MinIO 效能設定**：
```bash
# 設定環境變數以獲得更好的效能
export MINIO_API_REQUESTS_MAX=1000
export MINIO_API_REQUESTS_DEADLINE=10s
```

3. **硬體建議**：
- 使用 SSD 儲存以獲得更好的 I/O 效能
- 充足的 RAM（正式環境最少 4GB）
- 多個磁碟機用於分散式設定

## 監控

### 健康檢查
- **存活性**：`GET http://localhost:9002/minio/health/live`
- **就緒性**：`GET http://localhost:9002/minio/health/ready`

### 指標
MinIO 在以下位置提供 Prometheus 相容的指標：
- `GET http://localhost:9002/minio/v2/metrics/cluster`

## 備份和復原

### 資料備份
```bash
# 使用 MinIO client
./mc mirror local/my-bucket /backup/location

# 使用 rsync（用於檔案系統）
rsync -av /data/ /backup/minio-data/
```

### 災難復原
- 定期備份資料目錄
- 配置備份（access key、政策）
- 記錄您的 bucket 結構和政策

## 與應用程式整合

應用程式為 MinIO 操作提供以下端點：

- `POST /api/files/upload` - 上傳檔案
- `GET /api/files/download/{bucket}/{filename}` - 下載檔案
- `GET /api/files/list/{bucket}` - 列出檔案
- `GET /api/files/metadata/{bucket}/{filename}` - 取得檔案元資料
- `DELETE /api/files/{bucket}/{filename}` - 刪除檔案

詳細的 API 使用範例請參閱主要的 README.md。