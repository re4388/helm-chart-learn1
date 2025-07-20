package com.example.demoapp.adapter.web.controller;

import com.example.demoapp.infrastructure.observability.MetricsService;
import com.example.demoapp.infrastructure.observability.TracingService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Controller to demonstrate observability features
 * 展示可觀測性功能的控制器
 */
@Tag(name = "可觀測性", description = "提供生成範例指標、模擬追蹤和錯誤的端點")
@RestController
@RequestMapping("/api/observability")
public class ObservabilityController {
    
    private static final Logger logger = LoggerFactory.getLogger(ObservabilityController.class);
    
    private final MetricsService metricsService;
    private final TracingService tracingService;
    private final MeterRegistry meterRegistry;
    
    
    public ObservabilityController(MetricsService metricsService,
                                  TracingService tracingService,
                                  MeterRegistry meterRegistry) {
        this.metricsService = metricsService;
        this.tracingService = tracingService;
        this.meterRegistry = meterRegistry;
    }
    
    
    
    /**
     * Endpoint to simulate slow operations for tracing
     * 模擬慢操作以進行追蹤的端點
     */
    @Operation(summary = "模擬慢操作", description = "模擬一個帶有指定延遲的慢操作，用於追蹤測試。")
    @ApiResponse(responseCode = "200", description = "慢操作完成",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "500", description = "操作中斷")
    @GetMapping("/trace/slow-operation")
    public Map<String, Object> slowOperation(@Parameter(description = "模擬延遲的毫秒數", example = "1000") @RequestParam(defaultValue = "1000") int delayMs) {
        logger.info("Starting slow operation with delay: {}ms", delayMs);
        
        return tracingService.traceBusinessOperation("slow-operation", "demo", "test", () -> {
            tracingService.addSpanAttribute("operation.delay", String.valueOf(delayMs));
            tracingService.addBusinessContext("user123", "tenant456", "req-" + System.currentTimeMillis());
            
            try {
                // Simulate some work
                Thread.sleep(delayMs);
                
                // Simulate nested operations
                simulateNestedOperation("database-query", 200);
                simulateNestedOperation("external-api-call", 300);
                
                Map<String, Object> result = new HashMap<>();
                result.put("operation", "slow-operation");
                result.put("delay_ms", delayMs);
                result.put("status", "completed");
                result.put("timestamp", System.currentTimeMillis());
                
                logger.info("Completed slow operation after {}ms", delayMs);
                return result;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Operation interrupted", e);
            }
        });
    }
    
    /**
     * Endpoint to simulate errors for error tracking
     * 模擬錯誤以進行錯誤追蹤的端點
     */
    @Operation(summary = "模擬錯誤", description = "模擬一個錯誤條件，用於錯誤追蹤測試。")
    @ApiResponse(responseCode = "200", description = "錯誤模擬成功 (未觸發錯誤)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "500", description = "模擬錯誤觸發")
    @GetMapping("/trace/error-simulation")
    public Map<String, Object> errorSimulation(@Parameter(description = "是否觸發錯誤", example = "true") @RequestParam(defaultValue = "false") boolean shouldFail) {
        logger.info("Starting error simulation, shouldFail: {}", shouldFail);
        
        return tracingService.traceBusinessOperation("error-simulation", "demo", "test", () -> {
            tracingService.addSpanAttribute("simulation.shouldFail", String.valueOf(shouldFail));
            
            if (shouldFail) {
                logger.error("Simulating error condition");
                throw new RuntimeException("Simulated error for testing observability");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("operation", "error-simulation");
            result.put("status", "success");
            result.put("timestamp", System.currentTimeMillis());
            
            logger.info("Error simulation completed successfully");
            return result;
        });
    }
    
    /**
     * Endpoint to get current metrics summary
     * 取得當前指標摘要的端點
     */
    @Operation(summary = "獲取當前指標摘要", description = "返回應用程式的當前指標摘要，包括 JVM、CPU 和自定義指標。")
    @ApiResponse(responseCode = "200", description = "成功獲取指標摘要",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @GetMapping("/metrics/summary")
    public Map<String, Object> getMetricsSummary() {
        logger.debug("Retrieving metrics summary");
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get some basic metrics from the registry
        summary.put("jvm_memory_used", meterRegistry.get("jvm.memory.used").gauge().value());
        summary.put("jvm_memory_max", meterRegistry.get("jvm.memory.max").gauge().value());
        summary.put("process_cpu_usage", meterRegistry.get("process.cpu.usage").gauge().value());
        summary.put("system_cpu_usage", meterRegistry.get("system.cpu.usage").gauge().value());
        
        // Add custom metrics if they exist
        try {
            summary.put("post_operations_total", meterRegistry.get("demo_app_post_operations_total").counter().count());
            summary.put("file_operations_total", meterRegistry.get("demo_app_file_operations_total").counter().count());
            summary.put("business_events_total", meterRegistry.get("demo_app_business_events_total").counter().count());
        } catch (Exception e) {
            logger.debug("Some custom metrics not yet available: {}", e.getMessage());
        }
        
        summary.put("timestamp", System.currentTimeMillis());
        
        return summary;
    }
    
    private void simulateNestedOperation(String operationName, int delayMs) {
        tracingService.traceExternalCall("nested-service", operationName, () -> {
            try {
                Thread.sleep(delayMs);
                logger.debug("Completed nested operation: {} in {}ms", operationName, delayMs);
                return "success";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Nested operation interrupted", e);
            }
        });
    }
    
    
}