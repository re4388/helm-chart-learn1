package com.example.demoapp.infrastructure.service;

import com.example.demoapp.domain.model.HealthStatus;
import com.example.demoapp.domain.port.HealthService;
import org.springframework.stereotype.Service;

/**
 * Implementation of HealthService
 * HealthService 的實作
 */
@Service
public class HealthServiceImpl implements HealthService {
    
    private static final String SERVICE_NAME = "demo-app";
    
    @Override
    public HealthStatus getHealthStatus() {
        // In a real application, you might check database connections,
        // external services, etc. For this demo, we'll always return UP
        // 在真實應用程式中，你可能會檢查資料庫連線、外部服務等
        // 在這個示範中，我們總是回傳 UP
        return new HealthStatus(HealthStatus.Status.UP, SERVICE_NAME);
    }
}