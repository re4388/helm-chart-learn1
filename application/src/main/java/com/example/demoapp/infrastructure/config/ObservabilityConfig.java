package com.example.demoapp.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Observability configuration for custom metrics
 * 可觀測性配置，用於自定義指標
 */
@Configuration
public class ObservabilityConfig {
    
    /**
     * Counter for tracking post operations
     * 追蹤文章操作的計數器
     */
    @Bean
    public Counter postOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("demo_app_post_operations_total")
                .description("Total number of post operations")
                .tag("application", "demo-app")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking file operations
     * 追蹤檔案操作的計數器
     */
    @Bean
    public Counter fileOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("demo_app_file_operations_total")
                .description("Total number of file operations")
                .tag("application", "demo-app")
                .register(meterRegistry);
    }
    
    /**
     * Timer for tracking database operations
     * 追蹤資料庫操作的計時器
     */
    @Bean
    public Timer databaseOperationsTimer(MeterRegistry meterRegistry) {
        return Timer.builder("demo_app_database_operations_duration")
                .description("Duration of database operations")
                .tag("application", "demo-app")
                .register(meterRegistry);
    }
    
    /**
     * Timer for tracking MinIO operations
     * 追蹤 MinIO 操作的計時器
     */
    @Bean
    public Timer minioOperationsTimer(MeterRegistry meterRegistry) {
        return Timer.builder("demo_app_minio_operations_duration")
                .description("Duration of MinIO operations")
                .tag("application", "demo-app")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking business events
     * 追蹤業務事件的計數器
     */
    @Bean
    public Counter businessEventsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("demo_app_business_events_total")
                .description("Total number of business events")
                .tag("application", "demo-app")
                .register(meterRegistry);
    }
}