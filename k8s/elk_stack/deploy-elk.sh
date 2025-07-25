#!/bin/bash

# ELK Stack 部署腳本
set -e

echo "🚀 開始部署 ELK Stack..."

# 檢查 kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl 未安裝或不在 PATH 中"
    exit 1
fi

# 創建命名空間
echo "📁 創建 elk-stack 命名空間..."
kubectl apply -f namespace.yaml

# 等待命名空間創建
sleep 2

# 部署 Elasticsearch
echo "🔍 部署 Elasticsearch..."
kubectl apply -f elasticsearch.yaml

# 等待 Elasticsearch 啟動
echo "⏳ 等待 Elasticsearch 啟動..."
kubectl wait --for=condition=available deployment/elasticsearch -n elk-stack --timeout=300s

# 檢查 Elasticsearch 健康狀態
echo "🏥 檢查 Elasticsearch 健康狀態..."
kubectl port-forward -n elk-stack svc/elasticsearch 9200:9200 &
ES_PORT_FORWARD_PID=$!
sleep 5

# 等待 Elasticsearch 準備就緒
for i in {1..30}; do
    if curl -s "localhost:9200/_cluster/health" | grep -q "yellow\|green"; then
        echo "✅ Elasticsearch 健康狀態正常"
        break
    fi
    echo "⏳ 等待 Elasticsearch 準備就緒... ($i/30)"
    sleep 10
done

kill $ES_PORT_FORWARD_PID 2>/dev/null || true

# 部署 Logstash
echo "⚙️ 部署 Logstash..."
kubectl apply -f logstash-config.yaml
kubectl apply -f logstash.yaml

# 等待 Logstash 啟動
echo "⏳ 等待 Logstash 啟動..."
kubectl wait --for=condition=available deployment/logstash -n elk-stack --timeout=300s

# 部署 Filebeat
echo "📄 部署 Filebeat..."
kubectl apply -f filebeat-config.yaml
kubectl apply -f filebeat.yaml

# 等待 Filebeat 啟動
echo "⏳ 等待 Filebeat 啟動..."
kubectl rollout status daemonset/filebeat -n elk-stack --timeout=300s

# 部署 Kibana
echo "📊 部署 Kibana..."
kubectl apply -f kibana.yaml

# 等待 Kibana 啟動
echo "⏳ 等待 Kibana 啟動..."
kubectl wait --for=condition=available deployment/kibana -n elk-stack --timeout=300s

echo ""
echo "🎉 ELK Stack 部署完成！"
echo ""
echo "📊 服務狀態："
kubectl get pods -n elk-stack
echo ""
echo "🔗 訪問方式："
echo "Elasticsearch: kubectl port-forward -n elk-stack svc/elasticsearch 9200:9200"
echo "Kibana: kubectl port-forward -n elk-stack svc/kibana 5601:5601"
echo "Logstash API: kubectl port-forward -n elk-stack svc/logstash 9600:9600"
echo ""
echo "🎯 索引模式："
echo "- demo-app-api-logs-*     (API 請求日誌，已解析)"
echo "- demo-app-general-logs-* (一般應用程式日誌)"
echo ""
echo "💡 下一步："
echo "1. 在 Kibana 中創建索引模式"
echo "2. 設置儀表板和可視化"
echo "3. 配置告警和監控"