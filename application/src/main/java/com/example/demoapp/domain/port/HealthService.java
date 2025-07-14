package com.example.demoapp.domain.port;

import com.example.demoapp.domain.model.HealthStatus;

/**
 * Domain service interface for health check operations
 * 領域服務介面：健康檢查操作
 */
public interface HealthService {
    
    /**
     * Get current health status of the application
     * 取得應用程式目前的健康狀態
     */
    HealthStatus getHealthStatus();
}