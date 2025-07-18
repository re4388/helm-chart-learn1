# Demo Spring Boot 應用程式 - 完整部署指南

這是一個完整的 Spring Boot 微服務應用程式，展示了現代雲原生應用程式的最佳實踐，包含 MongoDB 資料庫、MinIO 物件儲存、Kubernetes 部署、ArgoCD GitOps 和完整的 CI/CD 流程。

## 🎯 專案概述

### 核心功能
- **RESTful API** - 完整的 REST API 服務
- **文章管理系統** - 使用 MongoDB 進行資料持久化
- **檔案管理系統** - 使用 MinIO 進行物件儲存
- **健康檢查** - Spring Boot Actuator 整合
- **可觀測性** - OpenTelemetry 追蹤和監控
- **六角架構** - 清潔架構設計模式

### 技術棧
- **後端**: Spring Boot 3.x, Java 17
- **資料庫**: MongoDB 7.0
- **物件儲存**: MinIO
- **容器化**: Docker & Docker Compose
- **編排**: Kubernetes
- **GitOps**: ArgoCD
- **CI/CD**: GitHub Actions
- **套件管理**: Helm Charts
- **可觀測性**: OpenTelemetry, Prometheus

## 📁 專案結構

```
├── application/                    # Spring Boot 應用程式
│   ├── src/main/java/             # Java 原始碼
│   ├── src/main/resources/        # 配置檔案
│   ├── docs/                      # 應用程式文檔
│   ├── scripts/                   # 測試腳本
│   └── Dockerfile                 # Docker 建置檔案
├── k8s/                           # Kubernetes 配置
│   ├── argocd/                    # ArgoCD 配置
│   └── helm-chart/                # Helm Charts
├── .github/workflows/             # GitHub Actions CI/CD
├── docker-compose.yml             # Docker Compose 配置
└── README.md                      # 本檔案
```

## 🚀 快速開始

### 方法一：Docker Compose（推薦新手）

1. **前置需求**
   ```bash
   # 確認工具版本
   docker --version          # 20.10+
   docker-compose --version  # 2.0+
   ```

2. **建置並啟動所有服務**
   ```bash
   # 建置應用程式
   cd application
   mvn clean package
   cd ..

   # 啟動所有服務
   docker-compose up -d
   ```

3. **驗證服務**
   ```bash
   # 檢查服務狀態
   docker-compose ps

   # 測試應用程式
   curl http://localhost:8080/actuator/health
   ```

4. **存取服務**
   - **應用程式**: http://localhost:8080
   - **MinIO 控制台**: http://localhost:9003 (minioadmin/minioadmin)
   - **MongoDB**: localhost:27017

### 方法二：Kubernetes + ArgoCD（推薦生產環境）

1. **前置需求**
   ```bash
   # 確認工具版本
   kubectl version --client   # 1.20+
   helm version              # 3.x
   ```

2. **安裝 ArgoCD**
   ```bash
   # 新增 Helm repository
   helm repo add argo https://argoproj.github.io/argo-helm
   helm repo update

   # 建立命名空間
   kubectl create namespace argocd

   # 安裝 ArgoCD
   helm install argocd argo/argo-cd \
     --namespace argocd \
     --values k8s/argocd/argocd-values.yaml
   ```

3. **部署應用程式**
   ```bash
   # 套用 ArgoCD 應用程式配置
   kubectl apply -f k8s/argocd/argocd-application.yaml
   ```

4. **存取 ArgoCD UI**
   ```bash
   # 取得 ArgoCD 密碼
   kubectl -n argocd get secret argocd-initial-admin-secret \
     -o jsonpath="{.data.password}" | base64 -d

   # 轉發埠號
   kubectl port-forward svc/argocd-server -n argocd 8080:443

   # 存取 UI: https://localhost:8080
   # 使用者名稱: admin
   # 密碼: 上面取得的密碼
   ```

## 🔧 開發指南

### 本地開發環境

1. **啟動依賴服務**
   ```bash
   # 只啟動 MongoDB 和 MinIO
   docker-compose up -d mongodb minio
   ```

2. **執行應用程式**
   ```bash
   cd application
   mvn spring-boot:run
   ```

3. **測試 API**
   ```bash
   # 基本健康檢查
   curl http://localhost:8080/actuator/health

   # 建立文章
   curl -X POST http://localhost:8080/api/posts \
     -H "Content-Type: application/json" \
     -d '{"title":"測試文章","content":"這是測試內容"}'

   # 上傳檔案
   curl -X POST \
     -F "file=@test.txt" \
     -F "bucket=test-bucket" \
     -F "uploadedBy=developer" \
     http://localhost:8080/api/files/upload
   ```

### 建置 Docker 映像

```bash
# 建置應用程式
cd application
mvn clean package

# 建置 Docker 映像
docker build -t your-username/demo-app:latest .

# 推送到 Docker Hub
docker push your-username/demo-app:latest
```

## 📚 API 文檔

### 基本端點

| 方法 | 端點 | 描述 |
|------|------|------|
| GET | `/` | 歡迎訊息 |
| GET | `/hello/{name}` | 個人化問候 |
| GET | `/actuator/health` | 健康檢查 |
| GET | `/actuator/metrics` | 應用程式指標 |

### 文章管理 API

| 方法 | 端點 | 描述 |
|------|------|------|
| GET | `/api/posts` | 取得所有文章 |
| POST | `/api/posts` | 建立新文章 |
| PUT | `/api/posts/{id}/publish` | 發布文章 |

### 檔案管理 API

| 方法 | 端點 | 描述 |
|------|------|------|
| POST | `/api/files/upload` | 上傳檔案 |
| GET | `/api/files/download/{bucket}/{filename}` | 下載檔案 |
| GET | `/api/files/list/{bucket}` | 列出檔案 |
| DELETE | `/api/files/{bucket}/{filename}` | 刪除檔案 |

### 可觀測性 API

| 方法 | 端點 | 描述 |
|------|------|------|
| GET | `/api/observability/metrics` | 自訂指標 |
| GET | `/api/observability/trace/business-operation` | 業務操作追蹤 |
| GET | `/api/observability/trace/error-simulation` | 錯誤模擬 |

## 🧪 測試指南

### 自動化測試腳本

```bash
# 檔案上傳測試
./application/scripts/test_file_upload.sh

# 使用自訂參數
./application/scripts/test_file_upload.sh my-bucket john.doe
```

### 手動測試範例

```bash
# 文章管理測試
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot 最佳實踐",
    "content": "這是一篇關於 Spring Boot 開發的文章...",
    "author": "開發者",
    "tags": ["spring-boot", "java", "微服務"]
  }'

# 檔案上傳測試
curl -X POST \
  -F "file=@README.md" \
  -F "bucket=documents" \
  -F "uploadedBy=tester" \
  http://localhost:8080/api/files/upload

# 效能測試
curl "http://localhost:8080/api/observability/metrics/performance-test?operations=100"
```

## 🏗️ 架構設計

### 六角架構（Hexagonal Architecture）

本應用程式採用六角架構設計，確保業務邏輯與外部依賴解耦：

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
└─────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────┐
│                  Domain Layer (領域層)                        │
├─────────────────────────────────────────────────────────────┤
│  Domain Models (領域模型)                                     │
│  - Post, FileMetadata                                       │
│  Business Rules (業務規則)                                    │
└─────────────────────────────────────────────────────────────┘
```

### 主要設計模式

- **依賴注入 (DI)**: Spring Framework
- **Repository 模式**: 資料存取抽象化
- **DTO 模式**: 資料傳輸物件
- **Builder 模式**: 物件建構
- **Strategy 模式**: 檔案儲存策略

## 🔄 CI/CD 流程

### GitHub Actions 工作流程

1. **Pull Request 檢查**
   - 程式碼品質檢查
   - 單元測試執行
   - 安全性掃描

2. **主分支部署**
   - 建置 Docker 映像
   - 推送到 Docker Hub
   - 觸發 ArgoCD 同步

3. **自動化測試**
   - 整合測試
   - API 測試
   - 效能測試

### 設定 CI/CD

1. **配置 Docker Hub 密鑰**
   ```bash
   # 在 GitHub Repository Settings > Secrets 中新增：
   DOCKER_USERNAME=your-docker-username
   DOCKER_PASSWORD=your-docker-password
   ```

2. **更新映像名稱**
   ```yaml
   # 在 k8s/helm-chart/demo-app/values.yaml 中更新：
   image:
     repository: your-username/demo-app
     tag: "latest"
   ```

## 🔍 可觀測性與監控

### OpenTelemetry 整合

應用程式整合了 OpenTelemetry 進行分散式追蹤：

```bash
# 啟用追蹤的環境變數
OTEL_SERVICE_NAME=demo-app
OTEL_RESOURCE_ATTRIBUTES=service.name=demo-app,service.version=1.0.0
```

### 監控端點

- **健康檢查**: `/actuator/health`
- **指標**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`
- **追蹤資訊**: `/api/observability/trace/*`

### 自訂指標

```bash
# 檢視自訂業務指標
curl http://localhost:8080/api/observability/metrics

# 效能測試指標
curl "http://localhost:8080/api/observability/metrics/performance-test?operations=50"
```

## 🛠️ 故障排除

### 常見問題

1. **Pod 無法啟動**
   ```bash
   # 檢查 Pod 狀態
   kubectl get pods -n demo-app
   
   # 查看 Pod 日誌
   kubectl logs -l app.kubernetes.io/name=demo-app -n demo-app
   
   # 檢查事件
   kubectl get events -n demo-app --sort-by='.lastTimestamp'
   ```

2. **MongoDB 連接問題**
   ```bash
   # 檢查 MongoDB 服務
   kubectl get svc demo-app-mongodb -n demo-app
   
   # 測試連接
   kubectl exec -it deployment/demo-app -n demo-app -- \
     curl -f http://demo-app-mongodb:27017
   ```

3. **MinIO 存取問題**
   ```bash
   # 檢查 MinIO 服務
   kubectl get svc demo-app-minio -n demo-app
   
   # 檢查 MinIO 日誌
   kubectl logs deployment/demo-app-minio -n demo-app
   ```

4. **映像拉取失敗**
   ```bash
   # 檢查映像是否存在
   docker pull your-username/demo-app:latest
   
   # 更新 Kubernetes 部署
   kubectl rollout restart deployment/demo-app -n demo-app
   ```

### 日誌檢查

```bash
# 應用程式日誌
kubectl logs -f deployment/demo-app -n demo-app

# ArgoCD 日誌
kubectl logs -f deployment/argocd-application-controller -n argocd

# 系統事件
kubectl get events --all-namespaces --sort-by='.lastTimestamp'
```

## 🔧 配置說明

### 環境變數

| 變數名稱 | 預設值 | 描述 |
|----------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `default` | Spring 設定檔 |
| `SPRING_DATA_MONGODB_HOST` | `localhost` | MongoDB 主機 |
| `SPRING_DATA_MONGODB_PORT` | `27017` | MongoDB 埠號 |
| `MINIO_ENDPOINT` | `http://localhost:9002` | MinIO 端點 |
| `MINIO_ACCESS_KEY` | `minioadmin` | MinIO 存取金鑰 |
| `MINIO_SECRET_KEY` | `minioadmin` | MinIO 秘密金鑰 |

### Helm 配置

主要配置檔案：
- `k8s/helm-chart/demo-app/values.yaml` - 開發環境配置
- `k8s/helm-chart/demo-app/values-production.yaml` - 生產環境配置

### ArgoCD 配置

- `k8s/argocd/argocd-application.yaml` - ArgoCD 應用程式定義
- `k8s/argocd/argocd-values.yaml` - ArgoCD 安裝配置

## 📈 效能調優

### 資源配置

```yaml
# Kubernetes 資源限制
resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 250m
    memory: 256Mi
```

### JVM 調優

```bash
# 生產環境 JVM 參數
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### MongoDB 調優

```yaml
# MongoDB 配置
mongodb:
  resources:
    limits:
      cpu: 500m
      memory: 512Mi
  persistence:
    size: 8Gi
```

## 🔒 安全性

### 安全配置

- **容器安全**: 非 root 使用者執行
- **網路安全**: Kubernetes NetworkPolicy
- **秘密管理**: Kubernetes Secrets
- **映像掃描**: GitHub Actions 安全掃描

### 最佳實踐

1. **定期更新依賴**
2. **使用最小權限原則**
3. **啟用 HTTPS**
4. **實施存取控制**
5. **監控安全事件**

## 🚀 部署策略

### 滾動更新

```bash
# 更新應用程式版本
kubectl set image deployment/demo-app demo-app=your-username/demo-app:v2.0.0 -n demo-app

# 檢查滾動更新狀態
kubectl rollout status deployment/demo-app -n demo-app
```

### 藍綠部署

```bash
# 使用 ArgoCD 進行藍綠部署
kubectl patch application demo-app -n argocd --type='merge' \
  -p='{"spec":{"source":{"targetRevision":"v2.0.0"}}}'
```

### 金絲雀部署

```yaml
# 在 values.yaml 中配置金絲雀部署
replicaCount: 3
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
```

## 📞 支援與貢獻

### 取得協助

- **GitHub Issues**: 回報問題或功能請求
- **討論區**: 技術討論和問答
- **文檔**: 查看詳細的技術文檔

### 貢獻指南

1. Fork 專案
2. 建立功能分支
3. 提交變更
4. 建立 Pull Request
5. 等待程式碼審查

### 開發環境設定

```bash
# 複製專案
git clone https://github.com/your-username/demo-app.git
cd demo-app

# 安裝依賴
cd application
mvn clean install

# 執行測試
mvn test

# 啟動開發環境
docker-compose up -d
mvn spring-boot:run
```

## 📄 授權條款

本專案採用 MIT 授權條款。詳細資訊請參閱 [LICENSE](LICENSE) 檔案。

## 🙏 致謝

感謝以下開源專案和社群：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MongoDB](https://www.mongodb.com/)
- [MinIO](https://min.io/)
- [Kubernetes](https://kubernetes.io/)
- [ArgoCD](https://argoproj.github.io/argo-cd/)
- [Helm](https://helm.sh/)
- [OpenTelemetry](https://opentelemetry.io/)

---

**🎉 恭喜！** 您現在擁有一個完整的雲原生應用程式部署解決方案！

如有任何問題或建議，歡迎建立 Issue 或 Pull Request。