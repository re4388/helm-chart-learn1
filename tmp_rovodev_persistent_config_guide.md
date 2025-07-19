# 持久性配置指南

## ⚠️ kubectl patch 的問題

`kubectl patch` 命令只是**臨時修改**，會在以下情況被重置：

1. 推送新的 commit 到 GitHub
2. ArgoCD 重新同步
3. 重新部署應用程式

## ✅ 正確的持久性做法

### 方法 1: 修改 values.yaml (推薦)

我已經幫你修改了 `k8s/helm-chart/demo-app/values.yaml`：

```yaml
otelCollector:
  enabled: true   # 已改為 true
  backend: "local"
```

### 方法 2: 使用 ArgoCD Application 的 parameters

在 `k8s/argocd/argocd-application.yaml` 中添加：

```yaml
spec:
  source:
    helm:
      parameters:
        - name: otelCollector.enabled
          value: "true"
        - name: otelCollector.backend
          value: "local"
```

## 🚀 提交變更到 Git

```bash
# 檢查修改
git status

# 添加修改的文件
git add k8s/helm-chart/demo-app/values.yaml

# 提交變更
git commit -m "Enable OpenTelemetry Collector for persistent monitoring"

# 推送到 GitHub
git push origin main
```

## 🔄 ArgoCD 會自動同步

推送後，ArgoCD 會：
1. 檢測到 Git 倉庫的變更
2. 自動同步新的配置
3. 部署 OpenTelemetry Collector

## 🔍 驗證持久性

```bash
# 檢查 ArgoCD Application 狀態
kubectl get application demo-app -n argocd

# 檢查 OTel Collector Pod
kubectl get pods -n demo-app -l app.kubernetes.io/component=otel-collector

# 檢查 Git 中的配置
git log --oneline -5
```

## 💡 最佳實踐

1. **總是修改 Git 中的配置文件**
2. **使用 kubectl patch 只用於測試**
3. **重要變更都要提交到版本控制**
4. **使用 ArgoCD 的 GitOps 工作流程**

## 🛠️ 如果需要臨時測試

如果你想要臨時測試不同的配置：

```bash
# 臨時啟用 (測試用)
kubectl patch application demo-app -n argocd --type='merge' -p='...'

# 測試完成後，重新同步 Git 配置
kubectl patch application demo-app -n argocd --type='merge' -p='{
  "operation": {
    "sync": {}
  }
}'
```

這樣可以確保最終配置與 Git 倉庫一致。