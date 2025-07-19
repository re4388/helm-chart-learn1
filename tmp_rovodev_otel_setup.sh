#!/bin/bash

echo "=== OpenTelemetry Collector 設定助手 ==="

echo "請選擇你想要連接的監控系統："
echo "1) Grafana + Prometheus (本地或雲端)"
echo "2) Jaeger (分散式追蹤)"
echo "3) Datadog"
echo "4) New Relic"
echo "5) 自定義 OTLP 端點"
echo "6) 查看當前集群中的監控服務"

read -p "請輸入選項 (1-6): " choice

case $choice in
  1)
    echo "配置 Grafana + Prometheus..."
    echo "需要 Prometheus 端點 URL (例如: http://prometheus-server:9090)"
    read -p "Prometheus URL: " prometheus_url
    ;;
  2)
    echo "配置 Jaeger..."
    echo "需要 Jaeger OTLP 端點 (例如: http://jaeger-collector:4317)"
    read -p "Jaeger OTLP URL: " jaeger_url
    ;;
  3)
    echo "配置 Datadog..."
    read -p "Datadog API Key: " datadog_key
    read -p "Datadog Site (例如: datadoghq.com): " datadog_site
    ;;
  4)
    echo "配置 New Relic..."
    read -p "New Relic License Key: " newrelic_key
    ;;
  5)
    echo "配置自定義 OTLP 端點..."
    read -p "OTLP 端點 URL: " custom_url
    read -p "是否需要認證? (y/n): " need_auth
    ;;
  6)
    echo "查看集群中的監控服務..."
    kubectl get services -A | grep -E "(grafana|prometheus|jaeger|datadog|newrelic|otel)"
    ;;
  *)
    echo "無效選項"
    exit 1
    ;;
esac

echo "配置選擇已記錄，請繼續執行後續步驟..."