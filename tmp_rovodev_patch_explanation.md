# kubectl patch 命令詳解

## 🎯 命令作用

```bash
kubectl patch application demo-app -n argocd --type='merge' -p='{...}'
```

這個命令的作用是**動態修改 ArgoCD Application 的配置**，而不需要直接編輯 YAML 文件。

## 📋 命令分解

### 1. `kubectl patch`
- Kubernetes 的資源修改命令
- 可以部分更新資源，而不需要替換整個資源

### 2. `application demo-app -n argocd`
- 目標資源: ArgoCD Application 名為 "demo-app"
- 命名空間: argocd

### 3. `--type='merge'`
- 使用 merge 策略
- 將新的配置與現有配置合併
- 不會覆蓋其他現有的參數

### 4. `-p='{...}'`
- patch 的內容 (JSON 格式)
- 指定要修改的具體配置

## 🔧 修改的具體內容

```json
{
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
}
```

### 這等同於在 Helm values.yaml 中設定:
```yaml
otelCollector:
  enabled: true
  backend: local
```

## 🔄 執行流程

1. **kubectl patch** 修改 ArgoCD Application
2. **ArgoCD** 檢測到配置變更
3. **ArgoCD** 自動觸發同步 (如果啟用了 auto-sync)
4. **Helm** 使用新的參數重新部署應用程式
5. **OpenTelemetry Collector** 被啟用並部署

## 🆚 其他方法比較

### 方法 1: 直接編輯 values.yaml
```bash
# 編輯文件
vim k8s/helm-chart/demo-app/values.yaml
# 提交到 Git
git add . && git commit -m "Enable OTel Collector"
git push
# 等待 ArgoCD 同步
```

### 方法 2: 使用 kubectl patch (我們使用的方法)
```bash
# 一行命令立即生效
kubectl patch application demo-app -n argocd --type='merge' -p='{...}'
```

### 方法 3: 使用 ArgoCD CLI
```bash
argocd app set demo-app -p otelCollector.enabled=true -p otelCollector.backend=local
```

## ✅ 優勢

1. **立即生效**: 不需要 Git 提交和推送
2. **精確控制**: 只修改特定參數
3. **不影響其他配置**: merge 策略保留其他設定
4. **可撤銷**: 可以用同樣方式改回來

## 🔍 驗證修改

### 檢查 Application 參數:
```bash
kubectl get application demo-app -n argocd -o jsonpath='{.spec.source.helm.parameters}' | jq .
```

### 檢查同步狀態:
```bash
kubectl get application demo-app -n argocd -o jsonpath='{.status.sync.status}'
```

### 檢查 OTel Collector Pod:
```bash
kubectl get pods -n demo-app -l app.kubernetes.io/component=otel-collector
```

## 🔄 如何撤銷

如果想要停用 OTel Collector:
```bash
kubectl patch application demo-app -n argocd --type='merge' -p='{
  "spec": {
    "source": {
      "helm": {
        "parameters": [
          {"name": "otelCollector.enabled", "value": "false"}
        ]
      }
    }
  }
}'
```