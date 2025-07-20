# 日誌系統部署指南

這個目錄包含了完整的 Fluentd + Elasticsearch 日誌收集系統的 Kubernetes 配置檔案。

## 檔案說明

- `elasticsearch-deployment.yaml` - Elasticsearch 部署配置
- `elasticsearch-service.yaml` - Elasticsearch 服務配置
- `fluentd-configmap.yaml` - Fluentd 配置檔案（使用最新的工作配置）
- `fluentd-daemonset.yaml` - Fluentd DaemonSet 部署配置
- `fluentd-serviceaccount.yaml` - Fluentd ServiceAccount
- `fluentd-rbac.yaml` - Fluentd RBAC 權限配置

## 部署順序

建議按照以下順序部署：

1. 首先部署 Elasticsearch：
   ```bash
   kubectl apply -f elasticsearch-deployment.yaml
   kubectl apply -f elasticsearch-service.yaml
   ```

2. 等待 Elasticsearch 啟動後，部署 Fluentd：
   ```bash
   kubectl apply -f fluentd-serviceaccount.yaml
   kubectl apply -f fluentd-rbac.yaml
   kubectl apply -f fluentd-configmap.yaml
   kubectl apply -f fluentd-daemonset.yaml
   ```

3. 或者一次性部署所有組件：
   ```bash
   kubectl apply -f k8s/log_infra/
   ```

## 驗證部署

檢查所有 Pod 是否正常運行：
```bash
kubectl get pods -n monitoring
```

檢查 Elasticsearch 是否可以訪問：
```bash
kubectl port-forward -n monitoring svc/elasticsearch 9200:9200
curl http://localhost:9200
```

## 特色功能

- 使用最新的 Fluentd 配置，包含完整的 JSON 日誌解析
- 正確的 @timestamp 時間戳處理
- 完整的 Kubernetes metadata 收集
- 結構化的日誌欄位提取（level, message, service, traceId 等）
- 按日期分割的 Elasticsearch 索引