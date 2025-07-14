package com.example.demoapp.adapter.web.dto;

import com.example.demoapp.domain.model.HealthStatus;

/**
 * Response DTO for health check endpoints
 * 健康檢查端點的回應 DTO
 */
public record HealthResponse(String status, String service) {

    public static HealthResponse fromDomain(HealthStatus healthStatus) {
        return new HealthResponse(
                healthStatus.getStatus().name(),
                healthStatus.getServiceName()
        );
    }
}