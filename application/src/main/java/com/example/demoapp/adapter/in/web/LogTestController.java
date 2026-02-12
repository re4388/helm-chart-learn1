package com.example.demoapp.adapter.in.web;

import com.example.demoapp.infrastructure.logging.ApiLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用於測試日誌功能的控制器
 */
@Tag(name = "日誌測試", description = "用於測試 AOP 日誌功能的 API")
@RestController
@RequestMapping("/api/log-test")
public class LogTestController {

    @Operation(summary = "基本日誌測試", description = "測試基本的 API 日誌記錄功能")
    @GetMapping("/basic")
    public ResponseEntity<Map<String, Object>> basicLogTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "基本日誌測試成功");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "慢查詢測試", description = "模擬慢查詢以測試慢查詢日誌記錄")
    @ApiLog(description = "慢查詢測試", slowQueryThreshold = 500)
    @GetMapping("/slow")
    public ResponseEntity<Map<String, Object>> slowQueryTest(@RequestParam(defaultValue = "1000") long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "慢查詢測試完成");
        response.put("delay", delay);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "帶參數的日誌測試", description = "測試帶有請求參數的日誌記錄")
    @ApiLog(logArgs = true, logResult = true)
    @PostMapping("/with-params")
    public ResponseEntity<Map<String, Object>> logTestWithParams(
            @RequestParam String name,
            @RequestParam(defaultValue = "18") int age,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "參數日誌測試成功");
        response.put("receivedName", name);
        response.put("receivedAge", age);
        response.put("receivedBody", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "錯誤處理測試", description = "測試異常情況下的日誌記錄")
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> errorTest(@RequestParam(defaultValue = "false") boolean throwError) {
        if (throwError) {
            throw new RuntimeException("這是一個測試異常");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "錯誤測試成功（沒有拋出異常）");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "無日誌記錄測試", description = "測試不記錄參數和結果的情況")
    @ApiLog(logArgs = false, logResult = false, description = "簡化日誌記錄")
    @GetMapping("/minimal")
    public ResponseEntity<Map<String, Object>> minimalLogTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "最小日誌測試成功");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}