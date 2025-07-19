#!/bin/bash

echo "=== OpenTelemetry Collector 部署助手 ==="
echo ""

# 顯示選項
echo "請選擇你想要連接的監控系統："
echo "1) 本地 Prometheus (適合測試)"
echo "2) Grafana Cloud"
echo "3) Datadog"
echo "4) New Relic"
echo "5) 自定義 OTLP 端點"
echo ""

read -p "請輸入選項 (1-5): " choice

case $choice in
  1)
    echo "配置本地 Prometheus..."
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
    echo "✅ 本地 Prometheus 配置完成！"
    echo "📊 指標將在 http://demo-app-otel-collector:8889/metrics 提供"
    ;;
    
  2)
    echo "配置 Grafana Cloud..."
    read -p "Prometheus Push URL: " prometheus_url
    read -p "Username: " username
    read -s -p "Password: " password
    echo ""
    
    # 創建 base64 編碼的認證
    basic_auth=$(echo -n "$username:$password" | base64 -w 0)
    
    kubectl patch application demo-app -n argocd --type='merge' -p='{
      "spec": {
        "source": {
          "helm": {
            "parameters": [
              {"name": "otelCollector.enabled", "value": "true"},
              {"name": "otelCollector.backend", "value": "grafana-cloud"},
              {"name": "otelCollector.exporters", "value": "logging,prometheusremotewrite"},
              {"name": "otelCollector.grafanaCloud.prometheusUrl", "value": "'$prometheus_url'"},
              {"name": "otelCollector.grafanaCloud.basicAuth", "value": "'$basic_auth'"}
            ]
          }
        }
      }
    }'
    echo "✅ Grafana Cloud 配置完成！"
    ;;
    
  3)
    echo "配置 Datadog..."
    read -p "Datadog API Key: " api_key
    read -p "Datadog Site (預設: datadoghq.com): " site
    site=${site:-datadoghq.com}
    
    kubectl patch application demo-app -n argocd --type='merge' -p='{
      "spec": {
        "source": {
          "helm": {
            "parameters": [
              {"name": "otelCollector.enabled", "value": "true"},
              {"name": "otelCollector.backend", "value": "datadog"},
              {"name": "otelCollector.exporters", "value": "logging,datadog"},
              {"name": "otelCollector.datadog.apiKey", "value": "'$api_key'"},
              {"name": "otelCollector.datadog.site", "value": "'$site'"}
            ]
          }
        }
      }
    }'
    echo "✅ Datadog 配置完成！"
    ;;
    
  4)
    echo "配置 New Relic..."
    read -p "New Relic License Key: " license_key
    
    kubectl patch application demo-app -n argocd --type='merge' -p='{
      "spec": {
        "source": {
          "helm": {
            "parameters": [
              {"name": "otelCollector.enabled", "value": "true"},
              {"name": "otelCollector.backend", "value": "newrelic"},
              {"name": "otelCollector.exporters", "value": "logging,otlphttp"},
              {"name": "otelCollector.newrelic.licenseKey", "value": "'$license_key'"}
            ]
          }
        }
      }
    }'
    echo "✅ New Relic 配置完成！"
    ;;
    
  5)
    echo "配置自定義 OTLP 端點..."
    read -p "OTLP 端點 URL: " endpoint
    read -p "是否使用 TLS? (y/n): " use_tls
    
    if [[ $use_tls == "n" || $use_tls == "N" ]]; then
      insecure="true"
    else
      insecure="false"
    fi
    
    kubectl patch application demo-app -n argocd --type='merge' -p='{
      "spec": {
        "source": {
          "helm": {
            "parameters": [
              {"name": "otelCollector.enabled", "value": "true"},
              {"name": "otelCollector.backend", "value": "custom"},
              {"name": "otelCollector.exporters", "value": "logging,otlp"},
              {"name": "otelCollector.custom.endpoint", "value": "'$endpoint'"},
              {"name": "otelCollector.custom.insecure", "value": "'$insecure'"}
            ]
          }
        }
      }
    }'
    echo "✅ 自定義端點配置完成！"
    ;;
    
  *)
    echo "❌ 無效選項"
    exit 1
    ;;
esac

echo ""
echo "🚀 正在部署 OpenTelemetry Collector..."
echo "⏳ 請等待 ArgoCD 同步..."

# 等待同步
sleep 10

echo ""
echo "📋 檢查部署狀態："
kubectl get application demo-app -n argocd -o jsonpath='{.status.sync.status}'
echo ""

echo ""
echo "🔍 檢查 OTel Collector Pod："
kubectl get pods -n demo-app -l app.kubernetes.io/component=otel-collector

echo ""
echo "📊 部署完成！你可以使用以下指令檢查："
echo "kubectl logs -n demo-app -l app.kubernetes.io/component=otel-collector"
echo "kubectl port-forward -n demo-app svc/demo-app-otel-collector 8889:8889"