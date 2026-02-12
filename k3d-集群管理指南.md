# K3d 集群管理指南

本指南說明如何安全地關閉和重新啟動整個 K3d 集群及其所有資源。

## 📊 目前運行的 K8s 資源

### Helm 部署的應用
- **ArgoCD** (argocd namespace) - GitOps 工具
- **Demo App** (demo-app namespace) - 主應用 + MongoDB + MinIO + OpenTelemetry
- **Mongo Express** (demo-app namespace) - MongoDB 管理界面
- **Prometheus Stack** (monitoring namespace) - 監控系統 (Prometheus + Grafana + AlertManager)
- **Traefik** (kube-system namespace) - 負載均衡器/Ingress Controller

### 其他資源
- **ELK Stack** (elk-stack namespace) - Elasticsearch + Logstash + Kibana + Filebeat

## 🛑 關閉所有資源的方法

### 方法 1：最簡單 - 直接關閉 k3d 集群（推薦）

```bash
# 停止整個 k3d 集群（最快最乾淨）
k3d cluster stop mycluster0

# 如果要完全刪除集群
k3d cluster delete mycluster0
```

**優點：**
- 最快速的方法
- 保留所有配置和數據
- 重啟後所有應用自動恢復

### 方法 2：逐步關閉應用（保留集群）

```bash
# 1. 卸載所有 Helm 應用
helm uninstall argocd -n argocd
helm uninstall demo-app -n demo-app
helm uninstall mongo-express -n demo-app
helm uninstall prometheus -n monitoring

# 2. 刪除 ELK Stack（非 Helm 管理）
kubectl delete namespace elk-stack

# 3. 刪除自定義命名空間
kubectl delete namespace argocd
kubectl delete namespace demo-app
kubectl delete namespace monitoring

# 注意：不要刪除 kube-system，那是系統必需的
```

**使用場景：**
- 需要保持集群運行但清理應用
- 測試重新部署流程

## 🔄 重新啟動所有資源

### 方法 1：如果使用了 k3d cluster stop

```bash
# 重新啟動 k3d 集群
k3d cluster start mycluster0

# 檢查集群狀態
kubectl get nodes
kubectl get pods --all-namespaces
```

### 方法 2：如果刪除了集群，需要重新創建

```bash
# 1. 重新創建 k3d 集群
k3d cluster create mycluster0

# 2. 重新安裝所有應用（按順序）

# 首先安裝 ArgoCD
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update
helm install argocd argo/argo-cd --create-namespace --namespace argocd

# 安裝 Prometheus 監控
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack \
  --create-namespace --namespace monitoring \
  -f k8s/monitoring/prometheus-values.yaml

# 安裝你的 Demo App
helm install demo-app k8s/helm-chart/demo-app \
  --create-namespace --namespace demo-app

# 安裝 Mongo Express
helm repo add cowboysysop https://cowboysysop.github.io/charts/
helm install mongo-express cowboysysop/mongo-express \
  --namespace demo-app

# 重新部署 ELK Stack
kubectl apply -f k8s/elk_stack/namespace.yaml
kubectl apply -f k8s/elk_stack/elasticsearch.yaml
kubectl apply -f k8s/elk_stack/logstash-config.yaml
kubectl apply -f k8s/elk_stack/logstash.yaml
kubectl apply -f k8s/elk_stack/kibana.yaml
kubectl apply -f k8s/elk_stack/filebeat-config.yaml
kubectl apply -f k8s/elk_stack/filebeat.yaml
```

### 方法 3：如果只是逐步關閉了應用

```bash
# 重新安裝所有 Helm 應用

# ArgoCD
helm repo add argo https://argoproj.github.io/argo-helm
helm install argocd argo/argo-cd --create-namespace --namespace argocd

# Prometheus
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack \
  --create-namespace --namespace monitoring \
  -f k8s/monitoring/prometheus-values.yaml

# Demo App
helm install demo-app k8s/helm-chart/demo-app \
  --create-namespace --namespace demo-app

# Mongo Express
helm repo add cowboysysop https://cowboysysop.github.io/charts/
helm install mongo-express cowboysysop/mongo-express \
  --namespace demo-app

# ELK Stack
bash k8s/elk_stack/deploy-elk.sh
```

## 📝 便利腳本

### 完全關閉腳本 (shutdown-all.sh)

```bash
#!/bin/bash
echo "正在關閉所有 K8s 資源..."

# 方法 1：直接停止集群（推薦）
k3d cluster stop mycluster0
echo "K3d 集群已停止"

# 方法 2：如果想保留集群但關閉應用，取消註釋以下行
# helm uninstall argocd -n argocd
# helm uninstall demo-app -n demo-app  
# helm uninstall mongo-express -n demo-app
# helm uninstall prometheus -n monitoring
# kubectl delete namespace elk-stack
# echo "所有應用已關閉，集群仍在運行"
```

### 重新啟動腳本 (restart-all.sh)

```bash
#!/bin/bash
echo "正在重新啟動所有資源..."

# 啟動 k3d 集群
k3d cluster start mycluster0

# 等待集群就緒
echo "等待集群就緒..."
sleep 30

# 檢查狀態
kubectl get nodes
kubectl get pods --all-namespaces

echo "集群已重新啟動，所有應用應該會自動恢復"
```

### 完整重建腳本 (rebuild-all.sh)

```bash
#!/bin/bash
echo "正在完整重建集群和所有應用..."

# 刪除現有集群
k3d cluster delete mycluster0

# 重新創建集群
k3d cluster create mycluster0

# 等待集群就緒
echo "等待集群就緒..."
sleep 60

# 添加 Helm repositories
helm repo add argo https://argoproj.github.io/argo-helm
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add cowboysysop https://cowboysysop.github.io/charts/
helm repo update

# 安裝 ArgoCD
echo "安裝 ArgoCD..."
helm install argocd argo/argo-cd --create-namespace --namespace argocd

# 安裝 Prometheus 監控
echo "安裝 Prometheus 監控..."
helm install prometheus prometheus-community/kube-prometheus-stack \
  --create-namespace --namespace monitoring \
  -f k8s/monitoring/prometheus-values.yaml

# 安裝 Demo App
echo "安裝 Demo App..."
helm install demo-app k8s/helm-chart/demo-app \
  --create-namespace --namespace demo-app

# 安裝 Mongo Express
echo "安裝 Mongo Express..."
helm install mongo-express cowboysysop/mongo-express \
  --namespace demo-app

# 部署 ELK Stack
echo "部署 ELK Stack..."
bash k8s/elk_stack/deploy-elk.sh

echo "所有應用已重新安裝完成！"
```

### 使用腳本

```bash
# 給腳本執行權限
chmod +x shutdown-all.sh
chmod +x restart-all.sh
chmod +x rebuild-all.sh

# 執行關閉
./shutdown-all.sh

# 執行重啟
./restart-all.sh

# 執行完整重建
./rebuild-all.sh
```

## 🔍 狀態檢查命令

### 檢查集群狀態

```bash
# 檢查 k3d 集群
k3d cluster list

# 檢查節點
kubectl get nodes

# 檢查所有命名空間
kubectl get namespaces

# 檢查所有 Pod
kubectl get pods --all-namespaces

# 檢查所有服務
kubectl get services --all-namespaces
```

### 檢查 Helm 部署

```bash
# 檢查所有 Helm 發布
helm list --all-namespaces

# 檢查特定命名空間的 Helm 發布
helm list -n argocd
helm list -n demo-app
helm list -n monitoring
```

### 檢查資源使用情況

```bash
# 檢查節點資源使用
kubectl top nodes

# 檢查 Pod 資源使用
kubectl top pods --all-namespaces

# 檢查存儲
kubectl get pv
kubectl get pvc --all-namespaces
```

## ⚡ 最佳實踐建議

### 1. 推薦使用方法 1（直接停止/啟動 k3d 集群）

**原因：**
- 最快速的方法
- 保留所有配置和數據
- 重啟後所有應用自動恢復
- 不會遺失任何設定
- 避免手動重新配置的錯誤

### 2. 數據持久化

- 你的應用使用了 PVC（Persistent Volume Claims）
- 數據會保留在 k3d 的 volume 中
- 即使重啟集群，數據也不會丟失

### 3. 監控和日誌

- Prometheus 會保留監控數據
- ELK Stack 會保留日誌數據
- Grafana 儀表板配置會保留

### 4. 網絡和存取

- Traefik 配置會自動恢復
- 所有 Ingress 規則會保持有效
- NodePort 和 LoadBalancer 服務會自動重新綁定

## 📋 故障排除

### 常見問題

1. **集群啟動後 Pod 處於 Pending 狀態**
   ```bash
   # 檢查節點狀態
   kubectl get nodes
   
   # 檢查 Pod 詳細信息
   kubectl describe pod <pod-name> -n <namespace>
   ```

2. **Helm 安裝失敗**
   ```bash
   # 更新 Helm repositories
   helm repo update
   
   # 檢查 Helm 狀態
   helm list --all-namespaces
   ```

3. **存儲問題**
   ```bash
   # 檢查 PV 和 PVC
   kubectl get pv
   kubectl get pvc --all-namespaces
   ```

4. **網絡連接問題**
   ```bash
   # 檢查服務
   kubectl get services --all-namespaces
   
   # 檢查 Ingress
   kubectl get ingress --all-namespaces
   ```

### 緊急恢復

如果遇到嚴重問題，可以使用完整重建：

```bash
# 完全重建（會丟失所有數據）
./rebuild-all.sh
```

## 📚 相關文件

- `setup_k8s_dashboard.md` - Kubernetes Dashboard 設置
- `k8s/monitoring/README.md` - 監控系統文檔
- `k8s/elk_stack/README.md` - ELK Stack 文檔
- `k8s/helm-chart/demo-app/` - Demo App Helm Chart
- `k8s/argocd/` - ArgoCD 配置文件