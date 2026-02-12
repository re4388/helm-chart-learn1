# API 日誌記錄指南

本指南說明如何使用 Spring AOP 為所有 API 添加 INFO 級別的日誌記錄功能。

## 功能特性

### 🚀 自動日誌記錄
- **自動攔截**：自動攔截所有控制器中的公共方法
- **請求追蹤**：為每個請求生成唯一的 RequestId
- **詳細信息**：記錄請求方法、URI、參數、客戶端 IP、User-Agent 等
- **執行時間**：自動計算和記錄 API 執行時間
- **異常處理**：自動記錄異常信息和錯誤堆疊

### 📊 日誌級別
- **INFO**：正常的 API 請求和響應
- **WARN**：慢查詢警告（可配置閾值）
- **ERROR**：API 執行異常

### 🎯 自定義控制
使用 `@ApiLog` 註解可以精細控制日誌行為：

```java
@ApiLog(
    logArgs = true,           // 是否記錄請求參數
    logResult = false,        // 是否記錄響應結果
    description = "用戶登錄",  // 自定義描述
    logExecutionTime = true,  // 是否記錄執行時間
    slowQueryThreshold = 500  // 慢查詢閾值（毫秒）
)
```

## 使用方法

### 1. 基本使用
所有控制器方法會自動記錄日誌，無需額外配置：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        // 自動記錄日誌
        return ResponseEntity.ok(userService.findById(id));
    }
}
```

### 2. 自定義日誌配置
使用 `@ApiLog` 註解進行自定義配置：

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @ApiLog(
        description = "用戶登錄",
        logArgs = false,  // 不記錄密碼等敏感參數
        slowQueryThreshold = 2000
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

### 3. 慢查詢監控
設置慢查詢閾值，超過閾值會記錄 WARN 級別日誌：

```java
@ApiLog(slowQueryThreshold = 500) // 500ms 閾值
@GetMapping("/heavy-operation")
public ResponseEntity<Result> heavyOperation() {
    // 如果執行時間超過 500ms，會記錄慢查詢警告
    return ResponseEntity.ok(performHeavyOperation());
}
```

## 日誌格式

### 請求開始日誌
```
INFO API_REQUEST_START - RequestId: abc12345, Method: GET, URI: /api/users/123, QueryString: N/A, ClientIP: 192.168.1.100, UserAgent: Mozilla/5.0..., Controller: UserController, Method: getUser, Args: [123]
```

### 請求成功日誌
```
INFO API_REQUEST_SUCCESS - RequestId: abc12345, Method: GET, URI: /api/users/123, StatusCode: 200, Duration: 45ms, Controller: UserController, Method: getUser, ResponseType: ResponseEntity
```

### 慢查詢警告
```
WARN SLOW_API_DETECTED - RequestId: abc12345, Method: GET, URI: /api/heavy-operation, Duration: 1200ms, Threshold: 500ms, Controller: HeavyController, Method: heavyOperation
```

### 異常錯誤日誌
```
ERROR API_REQUEST_ERROR - RequestId: abc12345, Method: POST, URI: /api/users, Duration: 15ms, Controller: UserController, Method: createUser, Error: IllegalArgumentException, Message: Invalid user data
```

## JSON 格式輸出

在 Kubernetes 環境中，日誌會以 JSON 格式輸出，便於 Fluentd 收集：

```json
{
  "@timestamp": "2025-07-20T10:30:45.123Z",
  "level": "INFO",
  "message": "API_REQUEST_SUCCESS - RequestId: abc12345, Method: GET, URI: /api/users/123, StatusCode: 200, Duration: 45ms",
  "service": "demo-app",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.example.demoapp.infrastructure.logging.ApiLoggingAspect",
  "traceId": "trace123",
  "spanId": "span456"
}
```

## 測試功能

### 1. 使用測試控制器
訪問 `/api/log-test/*` 端點來測試各種日誌功能：

```bash
# 基本日誌測試
curl http://localhost:8080/api/log-test/basic

# 慢查詢測試
curl http://localhost:8080/api/log-test/slow?delay=800

# 帶參數測試
curl -X POST http://localhost:8080/api/log-test/with-params?name=張三 \
  -H "Content-Type: application/json" \
  -d '{"role": "developer"}'

# 錯誤測試
curl http://localhost:8080/api/log-test/error?throwError=true
```

### 2. 使用測試腳本
執行提供的測試腳本：

```bash
chmod +x tmp_rovodev_test_api_logging.sh
./tmp_rovodev_test_api_logging.sh
```

## 配置說明

### 1. Maven 依賴
確保 `pom.xml` 中包含 AOP 依賴：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 2. 日誌配置
在 `logback-spring.xml` 中配置日誌級別：

```xml
<logger name="com.example.demoapp.infrastructure.logging.ApiLoggingAspect" level="INFO"/>
```

### 3. 啟用 AOP
確保有 `@EnableAspectJAutoProxy` 註解（已在 `LoggingConfiguration` 中配置）。

## 性能考慮

### 1. 參數清理
- 自動清理敏感信息（如文件上傳）
- 限制字符串長度避免日誌過大
- 對大型對象只記錄類型名稱

### 2. 異常處理
- 日誌記錄異常不會影響業務邏輯
- 使用 try-catch 包裝所有日誌操作

### 3. 性能影響
- 日誌記錄對性能影響極小（通常 < 1ms）
- 使用 MDC 進行線程安全的上下文管理

## 最佳實踐

### 1. 敏感信息處理
```java
@ApiLog(logArgs = false) // 不記錄包含密碼的請求參數
@PostMapping("/change-password")
public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
    // 處理密碼變更
}
```

### 2. 批量操作優化
```java
@ApiLog(
    description = "批量用戶導入",
    logArgs = false,  // 不記錄大量數據
    slowQueryThreshold = 5000  // 批量操作允許更長時間
)
@PostMapping("/batch-import")
public ResponseEntity<ImportResult> batchImport(@RequestBody List<User> users) {
    // 批量導入邏輯
}
```

### 3. 文件操作
```java
@ApiLog(
    description = "文件上傳",
    logArgs = false,  // 不記錄文件內容
    logResult = false // 不記錄響應內容
)
@PostMapping("/upload")
public ResponseEntity<UploadResult> uploadFile(@RequestParam MultipartFile file) {
    // 文件上傳邏輯
}
```

## 故障排除

### 1. 日誌未出現
- 檢查 AOP 是否正確啟用
- 確認日誌級別配置
- 驗證切點表達式是否匹配

### 2. 性能問題
- 調整 `slowQueryThreshold` 閾值
- 設置 `logArgs = false` 減少日誌量
- 檢查日誌輸出配置

### 3. JSON 格式問題
- 確認在 Kubernetes 環境中使用正確的 profile
- 檢查 `logback-spring.xml` 配置
- 驗證 JSON 編碼器依賴

## 監控和分析

### 1. 日誌查詢
在 Elasticsearch 中查詢 API 日誌：

```json
{
  "query": {
    "bool": {
      "must": [
        {"match": {"message": "API_REQUEST_SUCCESS"}},
        {"range": {"@timestamp": {"gte": "now-1h"}}}
      ]
    }
  }
}
```

### 2. 性能分析
查詢慢查詢：

```json
{
  "query": {
    "match": {"message": "SLOW_API_DETECTED"}
  },
  "sort": [{"@timestamp": {"order": "desc"}}]
}
```

### 3. 錯誤統計
統計 API 錯誤：

```json
{
  "query": {
    "match": {"message": "API_REQUEST_ERROR"}
  },
  "aggs": {
    "error_types": {
      "terms": {"field": "message.keyword"}
    }
  }
}
```