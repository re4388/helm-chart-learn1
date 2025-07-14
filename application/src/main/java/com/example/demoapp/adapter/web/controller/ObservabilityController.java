package com.example.demoapp.adapter.web.controller;

import com.example.demoapp.infrastructure.observability.MetricsService;
import com.example.demoapp.infrastructure.observability.TracingService;
import io.micrometer.core.instrument.MeterRegistry;
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
@RestController
@RequestMapping("/api/observability")
public class ObservabilityController {
    
    private static final Logger logger = LoggerFactory.getLogger(ObservabilityController.class);
    
    private final MetricsService metricsService;
    private final TracingService tracingService;
    private final MeterRegistry meterRegistry;
    private final Random random = new Random();
    
    public ObservabilityController(MetricsService metricsService,
                                  TracingService tracingService,
                                  MeterRegistry meterRegistry) {
        this.metricsService = metricsService;
        this.tracingService = tracingService;
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Endpoint to generate sample metrics
     * 生成範例指標的端點
     */
    @PostMapping("/metrics/generate")
    public Map<String, Object> generateMetrics(@RequestParam(defaultValue = "10") int count) {
        logger.info("Generating {} sample metrics", count);
        
        return tracingService.traceBusinessOperation("generate-metrics", "observability", "demo", () -> {
            tracingService.addSpanAttribute("metrics.count", String.valueOf(count));
            
            for (int i = 0; i < count; i++) {
                // Generate random business events
                String eventType = getRandomEventType();
                String category = getRandomCategory();
                metricsService.recordBusinessEvent(eventType, category);
                
                // Generate random file operations
                String fileOp = getRandomFileOperation();
                String status = random.nextBoolean() ? "success" : "error";
                long size = random.nextLong(1024 * 1024 * 10); // 0-10MB
                metricsService.recordFileOperation(fileOp, status, size);
                
                // Generate random post operations
                String postOp = getRandomPostOperation();
                metricsService.recordPostOperation(postOp, status);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("generated_metrics", count);
            result.put("timestamp", System.currentTimeMillis());
            result.put("status", "success");
            
            logger.info("Successfully generated {} metrics", count);
            return result;
        });
    }
    
    /**
     * Endpoint to simulate slow operations for tracing
     * 模擬慢操作以進行追蹤的端點
     */
    @GetMapping("/trace/slow-operation")
    public Map<String, Object> slowOperation(@RequestParam(defaultValue = "1000") int delayMs) {
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
    @GetMapping("/trace/error-simulation")
    public Map<String, Object> errorSimulation(@RequestParam(defaultValue = "false") boolean shouldFail) {
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
    
    private String getRandomEventType() {
        String[] events = {"user_login", "user_logout", "post_created", "post_updated", "file_uploaded", "file_downloaded"};
        return events[random.nextInt(events.length)];
    }
    
    private String getRandomCategory() {
        String[] categories = {"user_action", "content_management", "file_management", "system_event"};
        return categories[random.nextInt(categories.length)];
    }
    
    private String getRandomFileOperation() {
        String[] operations = {"upload", "download", "delete", "list"};
        return operations[random.nextInt(operations.length)];
    }
    
    private String getRandomPostOperation() {
        String[] operations = {"create", "update", "delete", "publish", "archive"};
        return operations[random.nextInt(operations.length)];
    }
}