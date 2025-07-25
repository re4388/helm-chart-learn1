# ELK Stack 部署指南

這個 ELK Stack 配置專為替代 FluentD 而設計，解決了我們在 FluentD 中遇到的所有問題。

## 🏗️ 架構概覽

```
Demo App → Filebeat → Logstash → Elasticsearch → Kibana
```

### 組件說明

- **Filebeat**: 收集容器日誌，替代 FluentD
- **Logstash**: 強大的日誌處理引擎，解析和轉換日誌
- **Elasticsearch**: 日誌存儲和搜索引擎
- **Kibana**: 可視化和儀表板

## 🚀 快速開始

### 1. 部署 ELK Stack
```bash
cd k8s/elk_stack
chmod +x *.sh
./deploy-elk.sh
```

### 2. 測試部署
```bash
./test-elk.sh
```

### 3. 訪問服務
```bash
# Kibana (Web UI)
kubectl port-forward -n elk-stack svc/kibana 5601:5601
# 瀏覽器訪問: http://localhost:5601

# Elasticsearch (API)
kubectl port-forward -n elk-stack svc/elasticsearch 9200:9200
# 訪問: http://localhost:9200

# Logstash (監控 API)
kubectl port-forward -n elk-stack svc/logstash 9600:9600
# 訪問: http://localhost:9600
```

## 📊 索引結構

### demo-app-api-logs-YYYY.MM.DD
專門處理 API 請求日誌，包含以下解析欄位：

#### API 請求欄位
- `api_request_id`: API 請求 ID
- `http_method`: HTTP 方法 (GET, POST, etc.)
- `uri`: 請求 URI
- `status_code`: HTTP 狀態碼
- `duration_ms`: 請求持續時間 (毫秒)
- `controller_name`: Spring Controller 名稱
- `controller_method`: Controller 方法
- `response_type`: 響應類型
- `client_ip`: 客戶端 IP
- `user_agent`: 用戶代理
- `error_message`: 錯誤訊息

#### 應用程式欄位
- `app_level`: 日誌級別
- `app_service`: 服務名稱
- `app_trace_id`: 分散式追蹤 ID
- `app_span_id`: Span ID
- `app_request_id`: 應用程式請求 ID
- `app_thread`: 執行緒名稱
- `app_logger`: Logger 名稱

#### Kubernetes 欄位
- `k8s_namespace`: 命名空間
- `k8s_pod_name`: Pod 名稱
- `k8s_container_name`: 容器名稱
- `k8s_node_name`: 節點名稱

### demo-app-general-logs-YYYY.MM.DD
一般應用程式日誌，未經特殊解析。

## 🔧 配置說明

### Filebeat 配置
- 只收集 `demo-app` 容器的日誌
- 自動添加 Kubernetes metadata
- 發送到 Logstash 進行處理

### Logstash 配置
- 解析 JSON 日誌格式
- 使用 Grok 解析 API 請求日誌
- 根據日誌類型路由到不同索引
- 自動創建 Elasticsearch 模板

### Elasticsearch 配置
- 單節點模式（適合開發/測試）
- 禁用安全功能（簡化配置）
- 自動索引模板管理

### Kibana 配置
- 連接到 Elasticsearch
- 禁用安全功能
- 提供 Web UI 訪問

## 🎯 支援的日誌格式

### API_REQUEST_SUCCESS
```
API_REQUEST_SUCCESS - RequestId: abc123, Method: GET, URI: /api/users, StatusCode: 200, Duration: 25ms, Controller: UserController, Method: getUser, ResponseType: UserResponse
```

### API_REQUEST_START
```
API_REQUEST_START - RequestId: abc123, Method: GET, URI: /api/users, QueryString: id=123, ClientIP: 192.168.1.1, UserAgent: Mozilla/5.0..., Controller: UserController, Method: getUser, Args: [123]
```

### API_REQUEST_ERROR
```
API_REQUEST_ERROR - RequestId: abc123, Method: POST, URI: /api/orders, Error: Invalid payment method
```

### SLOW_API_DETECTED
```
SLOW_API_DETECTED - RequestId: abc123, Duration: 5000ms, URI: /api/reports/heavy
```

## 📈 Kibana 設置

### 1. 創建索引模式
1. 訪問 Kibana (http://localhost:5601)
2. 進入 "Stack Management" → "Index Patterns"
3. 創建索引模式：
   - `demo-app-api-logs-*` (API 日誌)
   - `demo-app-general-logs-*` (一般日誌)
4. 選擇時間欄位：`@timestamp`

### 2. 探索數據
1. 進入 "Discover"
2. 選擇索引模式
3. 搜索和過濾日誌

### 3. 創建可視化
1. 進入 "Visualize"
2. 創建圖表：
   - API 響應時間趨勢
   - 錯誤率統計
   - 熱門 API 端點
   - 狀態碼分佈

## 🔍 查詢範例

### Elasticsearch 查詢

#### 查找慢查詢
```bash
curl "localhost:9200/demo-app-api-logs-*/_search" -H "Content-Type: application/json" -d '{
  "query": {
    "range": {
      "duration_ms": {
        "gte": 1000
      }
    }
  }
}'
```

#### 查找錯誤請求
```bash
curl "localhost:9200/demo-app-api-logs-*/_search" -H "Content-Type: application/json" -d '{
  "query": {
    "exists": {
      "field": "error_message"
    }
  }
}'
```

#### API 性能統計
```bash
curl "localhost:9200/demo-app-api-logs-*/_search" -H "Content-Type: application/json" -d '{
  "size": 0,
  "aggs": {
    "avg_duration_by_uri": {
      "terms": {
        "field": "uri"
      },
      "aggs": {
        "avg_duration": {
          "avg": {
            "field": "duration_ms"
          }
        }
      }
    }
  }
}'
```

### Kibana 查詢語法

#### KQL (Kibana Query Language)
```
# 查找特定 API
uri: "/api/users"

# 查找慢查詢
duration_ms >= 1000

# 查找錯誤
app_level: "ERROR"

# 組合查詢
uri: "/api/users" AND duration_ms >= 100
```

## 🚨 監控和告警

### 重要指標
- API 響應時間
- 錯誤率
- 請求量
- 系統資源使用

### 建議告警
- 平均響應時間 > 1000ms
- 錯誤率 > 5%
- 5xx 狀態碼數量異常
- 特定 API 端點異常

## 🔧 故障排除

### 檢查服務狀態
```bash
kubectl get pods -n elk-stack
kubectl logs -n elk-stack -l app=filebeat
kubectl logs -n elk-stack -l app=logstash
kubectl logs -n elk-stack -l app=elasticsearch
kubectl logs -n elk-stack -l app=kibana
```

### 檢查日誌流
```bash
# 檢查 Filebeat 是否收集日誌
kubectl logs -n elk-stack -l app=filebeat --tail=50

# 檢查 Logstash 處理狀態
curl "localhost:9600/_node/stats/pipelines?pretty"

# 檢查 Elasticsearch 索引
curl "localhost:9200/_cat/indices?v"
```

### 常見問題

#### 1. 沒有日誌進入 Elasticsearch
- 檢查 Filebeat 是否正在收集日誌
- 檢查 Logstash 配置是否正確
- 檢查網路連接

#### 2. Grok 解析失敗
- 檢查 Logstash 日誌中的 `_grokparsefailure` 標籤
- 驗證 Grok 模式是否匹配日誌格式
- 使用 Grok Debugger 測試模式

#### 3. 性能問題
- 調整 Logstash pipeline workers
- 增加 Elasticsearch 資源
- 優化索引設置

## 🔄 從 FluentD 遷移

### 遷移步驟
1. 部署 ELK Stack（並行運行）
2. 驗證日誌處理正確性
3. 比較索引數據
4. 切換應用程式日誌流
5. 停用 FluentD

### 優勢對比

| 特性 | FluentD | ELK Stack |
|------|---------|-----------|
| 日誌處理能力 | 有限 | 強大 |
| Grok 支援 | 需要 ES Pipeline | 原生支援 |
| 錯誤處理 | 基本 | 完善 |
| 調試能力 | 困難 | 容易 |
| 監控 | 有限 | 豐富 |
| 社群支援 | 好 | 優秀 |

## 📝 注意事項

1. **資源需求**: ELK Stack 比 FluentD 消耗更多資源
2. **複雜性**: 架構更複雜，但更強大
3. **維護**: 需要維護更多組件
4. **成本**: 可能增加基礎設施成本

## 🎯 下一步

1. 設置生產環境配置
2. 實施安全措施
3. 配置備份和恢復
4. 設置監控和告警
5. 優化性能和資源使用