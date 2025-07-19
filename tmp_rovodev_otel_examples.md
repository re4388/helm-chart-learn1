# OpenTelemetry Collector 外部監控系統配置範例

## 1. 本地 Prometheus + Grafana

```yaml
otelCollector:
  enabled: true
  backend: "local"
  exporters: "logging,prometheus"
```

然後可以配置 Grafana 從 `demo-app-otel-collector:8889` 抓取指標。

## 2. Grafana Cloud

```yaml
otelCollector:
  enabled: true
  backend: "grafana-cloud"
  exporters: "logging,prometheusremotewrite"
  grafanaCloud:
    prometheusUrl: "https://prometheus-prod-01-eu-west-0.grafana.net/api/prom/push"
    basicAuth: "base64_encoded_username:password"
```

## 3. Datadog

```yaml
otelCollector:
  enabled: true
  backend: "datadog"
  exporters: "logging,datadog"
  datadog:
    apiKey: "your-datadog-api-key"
    site: "datadoghq.com"
```

## 4. New Relic

```yaml
otelCollector:
  enabled: true
  backend: "newrelic"
  exporters: "logging,otlphttp"
  newrelic:
    licenseKey: "your-newrelic-license-key"
```

## 5. Jaeger (分散式追蹤)

```yaml
otelCollector:
  enabled: true
  backend: "jaeger"
  exporters: "logging,otlp"
  jaeger:
    endpoint: "http://jaeger-collector:4317"
    insecure: true
```

## 6. 自定義 OTLP 端點

```yaml
otelCollector:
  enabled: true
  backend: "custom"
  exporters: "logging,otlp"
  custom:
    endpoint: "https://your-custom-endpoint:4317"
    insecure: false
    headers:
      Authorization: "Bearer your-token"
      X-Custom-Header: "value"
```

## 快速部署指令

### 啟用本地 Prometheus
```bash
kubectl patch application demo-app -n argocd --type='merge' -p='{
  "spec": {
    "source": {
      "helm": {
        "parameters": [
          {"name": "otelCollector.enabled", "value": "true"},
          {"name": "otelCollector.backend", "value": "local"}
        ]
      }
    }
  }
}'
```

### 啟用 Datadog
```bash
kubectl patch application demo-app -n argocd --type='merge' -p='{
  "spec": {
    "source": {
      "helm": {
        "parameters": [
          {"name": "otelCollector.enabled", "value": "true"},
          {"name": "otelCollector.backend", "value": "datadog"},
          {"name": "otelCollector.datadog.apiKey", "value": "YOUR_API_KEY"},
          {"name": "otelCollector.exporters", "value": "logging,datadog"}
        ]
      }
    }
  }
}'
```

## 驗證部署

```bash
# 檢查 OTel Collector Pod
kubectl get pods -n demo-app -l app.kubernetes.io/component=otel-collector

# 檢查 OTel Collector 日誌
kubectl logs -n demo-app -l app.kubernetes.io/component=otel-collector

# 檢查 Prometheus 指標端點
kubectl port-forward -n demo-app svc/demo-app-otel-collector 8889:8889
curl http://localhost:8889/metrics
```