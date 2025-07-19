#!/bin/bash

echo "=== 設置本地 Kubernetes 監控堆疊 ==="
echo "這將在你的 K8s 集群中安裝："
echo "- Prometheus (指標收集)"
echo "- Grafana (視覺化)"
echo "- AlertManager (告警)"
echo "- Node Exporter (節點指標)"
echo "- Kube State Metrics (K8s 資源指標)"
echo ""

read -p "是否繼續安裝? (y/n): " confirm
if [[ $confirm != "y" && $confirm != "Y" ]]; then
    echo "安裝已取消"
    exit 0
fi

# 檢查 Helm 是否安裝
if ! command -v helm &> /dev/null; then
    echo "❌ Helm 未安裝，請先安裝 Helm"
    exit 1
fi

# 檢查 kubectl 是否可用
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ 無法連接到 Kubernetes 集群"
    exit 1
fi

echo ""
echo "🚀 開始安裝監控堆疊..."

# 執行安裝腳本
chmod +x k8s/monitoring/install-monitoring.sh
./k8s/monitoring/install-monitoring.sh

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 監控堆疊安裝成功！"
    
    # 安裝 ServiceMonitor
    echo ""
    echo "📊 安裝 demo-app ServiceMonitor..."
    kubectl apply -f k8s/monitoring/demo-app-servicemonitor.yaml
    
    # 啟用本地 OTel Collector
    echo ""
    echo "🔧 啟用本地 OpenTelemetry Collector..."
    kubectl patch application demo-app -n argocd --type='merge' -p='{
      "spec": {
        "source": {
          "helm": {
            "parameters": [
              {"name": "otelCollector.enabled", "value": "true"},
              {"name": "otelCollector.backend", "value": "local"},
              {"name": "otelCollector.exporters", "value": "logging,prometheus"}
            ]
          }
        }
      }
    }'
    
    echo ""
    echo "⏳ 等待 ArgoCD 同步..."
    sleep 15
    
    echo ""
    echo "🎉 設置完成！"
    echo ""
    echo "📋 訪問方式："
    echo ""
    echo "1. Grafana (視覺化):"
    echo "   kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80"
    echo "   訪問: http://localhost:3000"
    echo "   用戶名: admin"
    echo "   密碼: admin123"
    echo ""
    echo "2. Prometheus (指標查詢):"
    echo "   kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090"
    echo "   訪問: http://localhost:9090"
    echo ""
    echo "3. 檢查 demo-app 指標:"
    echo "   kubectl port-forward -n demo-app svc/demo-app 8080:8080"
    echo "   訪問: http://localhost:8080/actuator/prometheus"
    echo ""
    echo "📊 預設 Dashboard 已自動導入："
    echo "- JVM Dashboard (ID: 4701)"
    echo "- Spring Boot Dashboard (ID: 6756)"
    echo "- Kubernetes Dashboard (ID: 315)"
    echo ""
    echo "🔍 檢查所有服務狀態:"
    echo "kubectl get pods -n monitoring"
    echo "kubectl get pods -n demo-app"
    
else
    echo "❌ 安裝失敗，請檢查錯誤訊息"
    exit 1
fi