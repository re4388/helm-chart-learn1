package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.HealthStatus;
import com.example.demoapp.domain.port.HealthService;
import org.springframework.stereotype.Service;

/**
 * Use case for getting application health status
 * 用例：取得應用程式健康狀態
 */
@Service
public class GetHealthStatusUseCase {
    
    private final HealthService healthService;
    
    public GetHealthStatusUseCase(HealthService healthService) {
        this.healthService = healthService;
    }
    
    /**
     * Execute use case to get health status
     * 執行用例以取得健康狀態
     */
    public HealthStatus execute() {
        return healthService.getHealthStatus();
    }
}