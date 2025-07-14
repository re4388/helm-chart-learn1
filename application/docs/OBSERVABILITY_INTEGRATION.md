# 應用程式可觀測性整合說明

本文件說明如何在 Spring Boot 應用程式中整合 OpenTelemetry 可觀測性功能。

## 🎯 整合概覽

### 自動儀表化 (Auto-Instrumentation)
- **OpenTelemetry Java Agent**: 自動收集 JVM、HTTP、資料庫指標
- **Spring Boot Actuator**: 提供健康檢查和應用程式指標
- **Micrometer**: 與 Prometheus 整合的指標收集

### 手動儀表化 (Manual Instrumentation)
- **自定義指標**: 業務邏輯相關的計數器和計時器
- **自定義追蹤**: 業務操作的詳細追蹤
- **結構化日誌**: JSON 格式日誌與追蹤 ID 關聯

## 📊 已實現的指標

### JVM 指標 (自動收集)
```promql
# JVM 記憶體使用率
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# CPU 使用率
process_cpu_usage * 100

# 垃圾回收時間
rate(jvm_gc_pause_seconds_sum[5m])

# 執行緒數量
jvm_threads_live_threads
```

### HTTP 指標 (自動收集)
```promql
# 請求率
rate(http_server_requests_seconds_count[5m])

# 回應時間百分位數
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# 錯誤率
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m])
```

### 自定義業務指標
```promql
# 文章操作計數
demo_app_post_operations_total

# 檔案操作計數
demo_app_file_operations_total

# 業務事件計數
demo_app_business_events_total

# 資料庫操作時間
demo_app_database_operations_duration

# MinIO 操作時間
demo_app_minio_operations_duration
```

## 🔍 追蹤功能

### 自動追蹤的組件
- **HTTP 請求**: 所有 REST API 調用
- **MongoDB 查詢**: 資料庫操作追蹤
- **Spring Boot**: Controller、Service 方法
- **MinIO 操作**: 檔案上傳/下載操作

### 手動追蹤範例
```java
// 業務操作追蹤
@Service
public class PostService {
    
    @Autowired
    private TracingService tracingService;
    
    public Post createPost(String title, String content) {
        return tracingService.traceBusinessOperation(
            "create-post", "post", "new", 
            () -> {
                // 業務邏輯
                tracingService.addSpanAttribute("post.title", title);
                tracingService.addSpanAttribute("post.length", String.valueOf(content.length()));
                
                return postRepository.save(new Post(title, content));
            }
        );
    }
}
```

## 📝 日誌配置

### 結構化日誌格式
```json
{
  "@timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "message": "Processing request for user 123",
  "logger": "com.example.demoapp.adapter.web.controller.HelloController",
  "thread": "http-nio-8080-exec-1",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "spanId": "00f067aa0ba902b7",
  "service": "demo-app",
  "application": "demo-app",
  "environment": "dev"
}
```

### 日誌與追蹤關聯
- **Trace ID**: 自動注入到所有日誌記錄
- **Span ID**: 標識特定操作的日誌
- **MDC 上下文**: 業務上下文資訊

## 🛠️ 使用指南

### 1. 添加自定義指標

```java
@Service
public class MyService {
    
    @Autowired
    private MetricsService metricsService;
    
    public void performOperation() {
        // 記錄業務事件
        metricsService.recordBusinessEvent("operation_performed", "my_category");
        
        // 記錄操作時間
        Timer.Sample sample = metricsService.startDatabaseTimer("my_operation");
        try {
            // 執行操作
            doSomething();
            metricsService.stopDatabaseTimer(sample, "my_operation", "success");
        } catch (Exception e) {
            metricsService.stopDatabaseTimer(sample, "my_operation", "error");
            throw e;
        }
    }
}
```

### 2. 添加自定義追蹤

```java
@Service
public class MyService {
    
    @Autowired
    private TracingService tracingService;
    
    public Result processData(String dataId) {
        return tracingService.traceBusinessOperation(
            "process-data", "data", dataId,
            () -> {
                // 添加業務上下文
                tracingService.addBusinessContext("user123", "tenant456", "req-789");
                
                // 添加自定義屬性
                tracingService.addSpanAttribute("data.size", "1024");
                tracingService.addSpanAttribute("data.type", "json");
                
                // 執行業務邏輯
                return processDataInternal(dataId);
            }
        );
    }
}
```

### 3. 結構化日誌記錄

```java
@RestController
public class MyController {
    
    private static final Logger logger = LoggerFactory.getLogger(MyController.class);
    
    @GetMapping("/api/data/{id}")
    public ResponseEntity<Data> getData(@PathVariable String id) {
        // 結構化日誌會自動包含 traceId 和 spanId
        logger.info("Retrieving data for id: {}", id);
        
        try {
            Data data = dataService.findById(id);
            logger.info("Successfully retrieved data: id={}, size={}", id, data.getSize());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Failed to retrieve data: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
```

## 🧪 測試可觀測性功能

### 使用測試端點

```bash
# 生成範例指標
curl -X POST "http://localhost:8080/api/observability/metrics/generate?count=50"

# 測試慢操作追蹤
curl "http://localhost:8080/api/observability/trace/slow-operation?delayMs=2000"

# 測試錯誤追蹤
curl "http://localhost:8080/api/observability/trace/error-simulation?shouldFail=true"

# 查看指標摘要
curl "http://localhost:8080/api/observability/metrics/summary"
```

### 驗證指標收集

```bash
# 查看 Prometheus 指標端點
curl http://localhost:8080/actuator/prometheus

# 查看健康檢查
curl http://localhost:8080/actuator/health

# 查看應用程式資訊
curl http://localhost:8080/actuator/info
```

## 📈 監控儀表板

### Grafana 查詢範例

```promql
# 應用程式吞吐量
sum(rate(http_server_requests_seconds_count{job="demo-app"}[5m])) by (uri, method)

# 錯誤率
sum(rate(http_server_requests_seconds_count{job="demo-app",status=~"5.."}[5m])) / 
sum(rate(http_server_requests_seconds_count{job="demo-app"}[5m])) * 100

# 回應時間
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket{job="demo-app"}[5m])) by (le, uri)
)

# 自定義業務指標
rate(demo_app_post_operations_total[5m])
rate(demo_app_file_operations_total[5m])
```

### Kibana 日誌查詢

```json
{
  "query": {
    "bool": {
      "must": [
        {"match": {"service": "demo-app"}},
        {"range": {"@timestamp": {"gte": "now-1h"}}},
        {"exists": {"field": "traceId"}}
      ]
    }
  },
  "sort": [{"@timestamp": {"order": "desc"}}]
}
```

## 🔧 配置調優

### 效能考量

1. **批次處理**: OpenTelemetry Collector 使用批次處理減少網路開銷
2. **取樣率**: 在高流量環境中調整追蹤取樣率
3. **非同步日誌**: 使用 AsyncAppender 提高日誌效能

### 記憶體使用

```yaml
# application.yml
management:
  metrics:
    export:
      prometheus:
        step: 30s  # 減少指標收集頻率
    distribution:
      maximum-expected-value:
        http.server.requests: 10s  # 限制直方圖範圍
```

### 日誌輪轉

```xml
<!-- logback-spring.xml -->
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>/var/log/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <maxFileSize>100MB</maxFileSize>
    <maxHistory>7</maxHistory>  <!-- 保留 7 天 -->
    <totalSizeCap>1GB</totalSizeCap>  <!-- 總大小限制 -->
</rollingPolicy>
```

## 🚨 告警配置

### Prometheus 告警規則

```yaml
groups:
  - name: demo-app.rules
    rules:
      - alert: HighErrorRate
        expr: |
          (
            sum(rate(http_server_requests_seconds_count{job="demo-app",status=~"5.."}[5m])) /
            sum(rate(http_server_requests_seconds_count{job="demo-app"}[5m]))
          ) * 100 > 5
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}% for more than 2 minutes"
      
      - alert: HighResponseTime
        expr: |
          histogram_quantile(0.95, 
            sum(rate(http_server_requests_seconds_bucket{job="demo-app"}[5m])) by (le)
          ) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }}s"
```

## 📚 相關資源

### 官方文件
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)

### 最佳實踐
- [OpenTelemetry Best Practices](https://opentelemetry.io/docs/concepts/instrumentation/manual/)
- [Observability Patterns](https://microservices.io/patterns/observability/)

---

**🎉 現在你的應用程式已經完全整合了可觀測性功能！**