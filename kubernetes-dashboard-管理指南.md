# Kubernetes Dashboard 管理指南

本指南說明如何安全地關閉和重新設置 Kubernetes Dashboard，確保不會影響其他系統運行。

## 🛑 如何安全關閉 Kubernetes Dashboard

### 1. 停止 port-forward 連接（如果正在運行）

```bash
# 如果有正在運行的 port-forward，先停止它們
# 按 Ctrl+C 停止任何正在運行的 kubectl port-forward 命令
```

### 2. 使用 Helm 卸載 Dashboard

```bash
# 卸載 Kubernetes Dashboard
helm uninstall kubernetes-dashboard -n kubernetes-dashboard

# 刪除相關的 ServiceAccount 和 ClusterRoleBinding
kubectl delete -f k8s/argocd/dashboard-adminuser.yaml

# 刪除整個命名空間（這會清理所有相關資源）
kubectl delete namespace kubernetes-dashboard
```

## 🔄 如何重新設置 Kubernetes Dashboard

### 1. 重新安裝 Dashboard

```bash
# 確保 Helm repo 是最新的
helm repo update

# 重新安裝 Dashboard
helm install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard \
  --create-namespace \
  --namespace kubernetes-dashboard \
  --set service.type=NodePort

# 重新創建管理員用戶
kubectl apply -f k8s/argocd/dashboard-adminuser.yaml
```

### 2. 獲取新的訪問 Token

```bash
# 生成新的訪問 token
kubectl -n kubernetes-dashboard create token admin-user
```

### 3. 重新啟動訪問

```bash
# 方法 1：通過 Kong proxy（推薦）
kubectl port-forward -n kubernetes-dashboard svc/kubernetes-dashboard-kong-proxy 8443:443

# 方法 2：直接訪問 Web 服務
kubectl port-forward -n kubernetes-dashboard svc/kubernetes-dashboard-web 8080:8000
```

然後在瀏覽器中訪問：
- 方法 1：`https://localhost:8443`
- 方法 2：`http://localhost:8080`

## ✅ 安全性保證

### 為什麼關閉 Dashboard 不會破壞任何東西

1. **Dashboard 是獨立的**：Kubernetes Dashboard 運行在自己的命名空間中，不會影響你的應用程序
2. **只讀取集群信息**：Dashboard 只是一個管理界面，關閉它不會影響集群運行
3. **你的應用繼續運行**：所有在其他命名空間的應用（如你的 demo-app）會繼續正常運行
4. **監控系統不受影響**：你的 Prometheus、Grafana 等監控系統會繼續工作

## 📝 便利腳本

### 關閉腳本 (shutdown-dashboard.sh)

```bash
#!/bin/bash
echo "正在關閉 Kubernetes Dashboard..."
helm uninstall kubernetes-dashboard -n kubernetes-dashboard
kubectl delete -f k8s/argocd/dashboard-adminuser.yaml
kubectl delete namespace kubernetes-dashboard
echo "Dashboard 已安全關閉"
```

### 重啟腳本 (restart-dashboard.sh)

```bash
#!/bin/bash
echo "正在重新設置 Kubernetes Dashboard..."
helm repo update
helm install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard \
  --create-namespace \
  --namespace kubernetes-dashboard \
  --set service.type=NodePort
kubectl apply -f k8s/argocd/dashboard-adminuser.yaml
echo "Dashboard 已重新安裝，請運行以下命令獲取 token："
echo "kubectl -n kubernetes-dashboard create token admin-user"
```

### 使用腳本

```bash
# 給腳本執行權限
chmod +x shutdown-dashboard.sh
chmod +x restart-dashboard.sh

# 執行關閉
./shutdown-dashboard.sh

# 執行重啟
./restart-dashboard.sh
```

## 🔍 檢查狀態

### 檢查 Dashboard 是否正在運行

```bash
# 檢查命名空間是否存在
kubectl get namespace kubernetes-dashboard

# 檢查 Helm 安裝狀態
helm list -n kubernetes-dashboard

# 檢查服務狀態
kubectl get services -n kubernetes-dashboard

# 檢查 Pod 狀態
kubectl get pods -n kubernetes-dashboard
```

### 檢查相關資源

```bash
# 檢查 ServiceAccount
kubectl get serviceaccount admin-user -n kubernetes-dashboard

# 檢查 ClusterRoleBinding
kubectl get clusterrolebinding admin-user
```

## 📋 故障排除

### 常見問題

1. **無法訪問 Dashboard**
   - 確認 port-forward 正在運行
   - 檢查防火牆設置
   - 確認使用正確的瀏覽器（建議使用 Brave Browser）

2. **Token 無效**
   - 重新生成 token：`kubectl -n kubernetes-dashboard create token admin-user`

3. **Helm 安裝失敗**
   - 更新 Helm repo：`helm repo update`
   - 檢查 Kubernetes 集群連接

## 📚 相關文件

- `setup_k8s_dashboard.md` - 原始安裝指南
- `k8s/argocd/dashboard-adminuser.yaml` - 管理員用戶配置