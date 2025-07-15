# ArgoCD 安裝與設定指南

本指南將幫助您在 Kubernetes 集群中安裝 ArgoCD，並設定自動部署您的 Spring Boot 應用程式。

## 📋 前置需求

- Kubernetes 集群 (v1.20+)
- kubectl 已配置並可連接到集群
- Helm 3.x 已安裝

## 🚀 安裝步驟

### 1. 安裝 ArgoCD

```bash
# 建立 ArgoCD 命名空間
kubectl create namespace argocd

# 使用 Helm 安裝 ArgoCD
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# 安裝 ArgoCD
helm install argocd argo/argo-cd \
  --namespace argocd \
  --values argocd-values.yaml
```

### 2. 等待 ArgoCD 啟動

```bash
# 檢查 Pod 狀態
kubectl get pods -n argocd

# 等待所有 Pod 變為 Running 狀態
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s
```

### 3. 存取 ArgoCD UI

#### 方法 A: Port Forward (開發環境)
```bash
# 轉發 ArgoCD Server 端口
kubectl port-forward svc/argocd-server -n argocd 8080:443

# 在瀏覽器開啟: https://localhost:8080
# 忽略 SSL 警告 (自簽證書)
```

#### 方法 B: LoadBalancer (生產環境)
```bash
# 修改 Service 類型為 LoadBalancer
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'

# 取得外部 IP
kubectl get svc argocd-server -n argocd
```

### 4. 取得 ArgoCD 管理員密碼

```bash
# 取得初始管理員密碼
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo

# 登入資訊:
# 用戶名: admin
# 密碼: (上面命令的輸出)
```

### 5. 安裝 ArgoCD CLI (可選)

```bash
# macOS
brew install argocd

# Linux
curl -sSL -o argocd-linux-amd64 https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
sudo install -m 555 argocd-linux-amd64 /usr/local/bin/argocd
rm argocd-linux-amd64

# Windows (使用 Chocolatey)
choco install argocd-cli
```

### 6. 登入 ArgoCD CLI

```bash
# 登入 ArgoCD
argocd login localhost:8080

# 或使用外部 IP
argocd login <EXTERNAL-IP>

# 輸入用戶名和密碼
```

## 🔧 部署應用程式

### 1. 建立應用程式命名空間

```bash
# 建立應用程式命名空間
kubectl create namespace demo-app
```

### 2. 部署基礎設施 (MongoDB & MinIO)

```bash
# 部署 MongoDB
kubectl apply -f ../infrastructure/mongodb/

# 部署 MinIO
kubectl apply -f ../infrastructure/minio/

# 檢查部署狀態
kubectl get pods -n demo-app
```

### 3. 在 ArgoCD 中建立應用程式

#### 方法 A: 使用 UI
1. 開啟 ArgoCD UI
2. 點擊 "NEW APP"
3. 填入以下資訊：
   - **Application Name**: `demo-app`
   - **Project**: `default`
   - **Sync Policy**: `Automatic`
   - **Repository URL**: `https://github.com/re4388/helm-chart-learn1`
   - **Revision**: `main`
   - **Path**: `k8s/helm-charts/demo-app`
   - **Destination Cluster**: `https://kubernetes.default.svc`
   - **Namespace**: `demo-app`

#### 方法 B: 使用 CLI
```bash
argocd app create demo-app \
  --repo https://github.com/re4388/helm-chart-learn1 \
  --path k8s/helm-charts/demo-app \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace demo-app \
  --sync-policy automated \
  --auto-prune \
  --self-heal
```

#### 方法 C: 使用 YAML (推薦)
```bash
kubectl apply -f argocd-application.yaml
```

### 4. 同步應用程式

```bash
# 手動同步 (如果沒有啟用自動同步)
argocd app sync demo-app

# 檢查應用程式狀態
argocd app get demo-app
```

## 📊 監控與管理

### 檢查應用程式狀態

```bash
# 檢查 ArgoCD 應用程式
kubectl get applications -n argocd

# 檢查應用程式 Pod
kubectl get pods -n demo-app

# 檢查服務
kubectl get svc -n demo-app

# 檢查 Ingress (如果有)
kubectl get ingress -n demo-app
```

### 查看日誌

```bash
# 查看 Spring Boot 應用程式日誌
kubectl logs -f deployment/demo-app -n demo-app

# 查看 MongoDB 日誌
kubectl logs -f deployment/mongodb -n demo-app

# 查看 MinIO 日誌
kubectl logs -f deployment/minio -n demo-app
```

### 存取應用程式

```bash
# Port Forward 到 Spring Boot 應用程式
kubectl port-forward svc/demo-app -n demo-app 8080:8080

# 測試應用程式
curl http://localhost:8080/actuator/health

# Port Forward 到 MinIO Console
kubectl port-forward svc/minio-console -n demo-app 9001:9001

# 存取 MinIO Console: http://localhost:9001
```

## 🔄 自動部署流程

當您推送程式碼到 GitHub 時：

1. **GitHub Actions** 觸發 CI/CD
2. **建置並推送** Docker 映像檔到 Docker Hub
3. **ArgoCD** 檢測到 Git 倉庫變更
4. **自動同步** 最新的 Helm Chart
5. **部署更新** 到 Kubernetes 集群

## 🛠️ 故障排除

### ArgoCD Server 無法啟動
```bash
# 檢查 Pod 狀態
kubectl describe pod -l app.kubernetes.io/name=argocd-server -n argocd

# 檢查日誌
kubectl logs -l app.kubernetes.io/name=argocd-server -n argocd
```

### 應用程式同步失敗
```bash
# 檢查應用程式詳細資訊
argocd app get demo-app

# 檢查同步狀態
kubectl describe application demo-app -n argocd
```

### 無法存取 ArgoCD UI
```bash
# 檢查 Service
kubectl get svc argocd-server -n argocd

# 檢查 Port Forward
kubectl port-forward svc/argocd-server -n argocd 8080:443 --address 0.0.0.0
```

## 🔐 安全性建議

### 1. 變更預設密碼
```bash
# 使用 ArgoCD CLI 變更密碼
argocd account update-password
```

### 2. 設定 RBAC
```bash
# 建立自定義 RBAC 規則
kubectl apply -f argocd-rbac.yaml
```

### 3. 啟用 TLS
```bash
# 設定自定義 TLS 證書
kubectl create secret tls argocd-server-tls \
  --cert=path/to/cert.pem \
  --key=path/to/key.pem \
  -n argocd
```

## 📚 進階配置

### 多集群管理
```bash
# 新增外部集群
argocd cluster add <CONTEXT-NAME>
```

### Webhook 設定
```bash
# 設定 GitHub Webhook 以實現即時同步
# Webhook URL: https://<ARGOCD-SERVER>/api/webhook
```

### 自定義同步策略
```yaml
# 在 Application YAML 中設定
spec:
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
    - CreateNamespace=true
```

## 🎯 下一步

1. 設定監控 (Prometheus + Grafana)
2. 配置日誌聚合 (ELK Stack)
3. 實施備份策略
4. 設定災難恢復

## 📞 支援

如果遇到問題，請檢查：
- [ArgoCD 官方文檔](https://argo-cd.readthedocs.io/)
- [Helm 官方文檔](https://helm.sh/docs/)
- [Kubernetes 官方文檔](https://kubernetes.io/docs/)