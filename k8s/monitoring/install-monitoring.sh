#!/bin/bash

echo "=== 安裝本地 Prometheus + Grafana 監控堆疊 ==="

# 創建 monitoring namespace
echo "1. 創建 monitoring namespace..."
kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -

# 添加 Prometheus Community Helm Repository
echo "2. 添加 Helm Repository..."
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# 安裝 kube-prometheus-stack
echo "3. 安裝 kube-prometheus-stack..."
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --values k8s/monitoring/prometheus-values.yaml \
  --wait

# 等待所有 Pod 準備就緒
echo "4. 等待所有 Pod 準備就緒..."
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n monitoring --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s

# 顯示服務狀態
echo "5. 檢查服務狀態..."
kubectl get pods -n monitoring
echo ""
kubectl get svc -n monitoring

echo ""
echo "=== 安裝完成！==="
echo ""
echo "📊 Grafana 訪問方式："
echo "kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80"
echo "然後訪問: http://localhost:3000"
echo "用戶名: admin"
echo "密碼: admin123"
echo ""
echo "📈 Prometheus 訪問方式："
echo "kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090"
echo "然後訪問: http://localhost:9090"
echo ""
echo "🔔 AlertManager 訪問方式："
echo "kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-alertmanager 9093:9093"
echo "然後訪問: http://localhost:9093"