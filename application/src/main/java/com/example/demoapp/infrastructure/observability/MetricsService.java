package com.example.demoapp.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

/**
 * Service for recording custom metrics
 * 記錄自定義指標的服務
 */
@Service
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Record post operation metrics
     * 記錄文章操作指標
     */
    public void recordPostOperation(String operation, String status) {
        Counter.builder("demo_app_post_operations_total")
                .description("Total number of post operations")
                .tag("application", "demo-app")
                .tag("operation", operation)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record file operation metrics
     * 記錄檔案操作指標
     */
    public void recordFileOperation(String operation, String status, long sizeBytes) {
        Counter.builder("demo_app_file_operations_total")
                .description("Total number of file operations")
                .tag("application", "demo-app")
                .tag("operation", operation)
                .tag("status", status)
                .tag("size_category", categorizeSizeBytes(sizeBytes))
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record database operation timing
     * 記錄資料庫操作時間
     */
    public Timer.Sample startDatabaseTimer(String operation) {
        return Timer.start(meterRegistry);
    }
    
    public void stopDatabaseTimer(Timer.Sample sample, String operation, String status) {
        Timer timer = Timer.builder("demo_app_database_operations_duration")
                .description("Duration of database operations")
                .tag("application", "demo-app")
                .tag("operation", operation)
                .tag("status", status)
                .register(meterRegistry);
        sample.stop(timer);
    }
    
    /**
     * Record MinIO operation timing
     * 記錄 MinIO 操作時間
     */
    public Timer.Sample startMinioTimer(String operation) {
        return Timer.start(meterRegistry);
    }
    
    public void stopMinioTimer(Timer.Sample sample, String operation, String status) {
        Timer timer = Timer.builder("demo_app_minio_operations_duration")
                .description("Duration of MinIO operations")
                .tag("application", "demo-app")
                .tag("operation", operation)
                .tag("status", status)
                .register(meterRegistry);
        sample.stop(timer);
    }
    
    /**
     * Record business events
     * 記錄業務事件
     */
    public void recordBusinessEvent(String eventType, String category) {
        Counter.builder("demo_app_business_events_total")
                .description("Total number of business events")
                .tag("application", "demo-app")
                .tag("event_type", eventType)
                .tag("category", category)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Categorize file size for metrics
     * 為指標分類檔案大小
     */
    private String categorizeSizeBytes(long sizeBytes) {
        if (sizeBytes < 1024) {
            return "small";  // < 1KB
        } else if (sizeBytes < 1024 * 1024) {
            return "medium"; // < 1MB
        } else if (sizeBytes < 10 * 1024 * 1024) {
            return "large";  // < 10MB
        } else {
            return "xlarge"; // >= 10MB
        }
    }
}