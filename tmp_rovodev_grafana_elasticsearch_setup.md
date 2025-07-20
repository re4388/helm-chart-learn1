# Grafana Elasticsearch 資料源設置指南

## 📊 在 Grafana 中添加 Elasticsearch 資料源

### 步驟 1: 訪問 Grafana
```bash
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
```
訪問: http://localhost:3000
- 用戶名: `admin`
- 密碼: `admin123`

### 步驟 2: 添加資料源
1. 點擊左側選單 "Configuration" → "Data Sources"
2. 點擊 "Add data source"
3. 選擇 "Elasticsearch"

### 步驟 3: 基本設置
```yaml
Name: "Demo App Logs"
URL: "http://elasticsearch.monitoring.svc.cluster.local:9200"
Access: "Server (default)"
```

### 步驟 4: Elasticsearch 詳細設置
```yaml
Index name: "demo-app-logs-*"
Pattern: "Daily"
Time field name: "@timestamp"
Version: "8.0+"
Max concurrent Shard Requests: 5
```

### 步驟 5: 進階設置 (可選)
```yaml
# 如果需要認證 (目前不需要)
Basic Auth: 關閉
With Credentials: 關閉
TLS Client Auth: 關閉

# 日誌設置
Message field name: "message"
Level field name: "level"
```

## 🔍 常用 Lucene 查詢語法

### 基本查詢
```lucene
# 搜尋特定訊息
message:"Started DemoAppApplication"

# 搜尋錯誤日誌
level:ERROR

# 搜尋特定服務
service:"demo-app"

# 搜尋特定 logger
logger:"com.example.demoapp.*"
```

### 時間範圍查詢
```lucene
# 最近 15 分鐘的錯誤
level:ERROR AND @timestamp:[now-15m TO now]

# 特定時間範圍
@timestamp:[2025-07-20T08:00:00 TO 2025-07-20T09:00:00]
```

### 複合查詢
```lucene
# AND 查詢
level:ERROR AND service:"demo-app"

# OR 查詢
level:ERROR OR level:WARN

# NOT 查詢
NOT level:DEBUG

# 通配符查詢
message:*exception*

# 正則表達式
message:/.*[Ee]rror.*/
```

### 欄位存在性查詢
```lucene
# 有 traceId 的日誌
_exists_:traceId

# 沒有 traceId 的日誌
NOT _exists_:traceId
```

### 範圍查詢
```lucene
# HTTP 狀態碼範圍
http_status:[400 TO 599]

# 數值範圍
response_time:>1000
```

## 📈 建議的 Dashboard 面板

### 1. 日誌數量趨勢
```json
{
  "query": "*",
  "metrics": [
    {
      "type": "count",
      "field": "@timestamp"
    }
  ]
}
```

### 2. 錯誤日誌統計
```json
{
  "query": "level:ERROR",
  "metrics": [
    {
      "type": "count",
      "field": "@timestamp"
    }
  ]
}
```

### 3. 日誌級別分布
```json
{
  "query": "*",
  "bucketAggs": [
    {
      "type": "terms",
      "field": "level.keyword"
    }
  ]
}
```

### 4. 最新錯誤日誌表格
```json
{
  "query": "level:ERROR",
  "size": 100,
  "sort": [
    {
      "@timestamp": {
        "order": "desc"
      }
    }
  ]
}
```

## 🎯 實用的查詢範例

### 應用程式啟動相關
```lucene
message:"Started DemoAppApplication" OR message:"Tomcat started"
```

### HTTP 請求追蹤
```lucene
_exists_:trace_id AND (message:*request* OR message:*response*)
```

### 資料庫操作
```lucene
logger:*mongodb* OR message:*mongo*
```

### 效能相關
```lucene
message:*slow* OR message:*timeout* OR message:*performance*
```

### OpenTelemetry 追蹤
```lucene
_exists_:traceId AND _exists_:spanId
```

## 🔧 故障排除

### 如果看不到資料
1. 檢查索引是否存在:
```bash
curl 'http://localhost:9200/_cat/indices?v'
```

2. 檢查資料格式:
```bash
curl 'http://localhost:9200/demo-app-logs-*/_search?size=1'
```

3. 檢查時間欄位:
```lucene
@timestamp:[now-1h TO now]
```

### 常見問題
- **No data**: 檢查時間範圍和索引模式
- **Too many results**: 使用更具體的查詢條件
- **Slow queries**: 避免使用前綴通配符 `*term`