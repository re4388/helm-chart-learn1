#!/bin/bash

# ELK Stack 測試腳本
set -e

echo "🧪 測試 ELK Stack..."

# 檢查所有 Pod 狀態
echo "📊 檢查 Pod 狀態..."
kubectl get pods -n elk-stack

echo ""
echo "🔍 檢查 Elasticsearch 健康狀態..."
kubectl port-forward -n elk-stack svc/elasticsearch 9200:9200 &
ES_PID=$!
sleep 5

curl -s "localhost:9200/_cluster/health?pretty"
echo ""

echo "📈 檢查現有索引..."
curl -s "localhost:9200/_cat/indices?v"
echo ""

kill $ES_PID 2>/dev/null || true

echo "⚙️ 檢查 Logstash 狀態..."
kubectl port-forward -n elk-stack svc/logstash 9600:9600 &
LS_PID=$!
sleep 5

curl -s "localhost:9600/_node/stats/pipelines?pretty" | jq '.pipelines'
echo ""

kill $LS_PID 2>/dev/null || true

echo "📄 檢查 Filebeat 狀態..."
kubectl logs -n elk-stack -l app=filebeat --tail=10

echo ""
echo "🎯 觸發測試 API 請求..."
# 假設您的應用程式在 demo-app 命名空間
if kubectl get svc -n demo-app demo-app &>/dev/null; then
    kubectl port-forward -n demo-app svc/demo-app 8080:8080 &
    APP_PID=$!
    sleep 3
    
    echo "發送測試請求..."
    curl -s "localhost:8080/hello/ELK_STACK_TEST" && echo
    
    kill $APP_PID 2>/dev/null || true
    
    echo "⏳ 等待日誌處理..."
    sleep 30
    
    echo "🔍 檢查新索引..."
    kubectl port-forward -n elk-stack svc/elasticsearch 9200:9200 &
    ES_PID=$!
    sleep 5
    
    curl -s "localhost:9200/_cat/indices/demo-app-*?v"
    echo ""
    
    echo "🔍 搜索測試日誌..."
    curl -s "localhost:9200/demo-app-api-logs-*/_search?q=ELK_STACK_TEST&pretty" | jq '.hits.total.value'
    
    kill $ES_PID 2>/dev/null || true
fi

echo ""
echo "✅ ELK Stack 測試完成！"