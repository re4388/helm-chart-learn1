package com.example.demoapp.adapter.web.dto;

import com.example.demoapp.domain.model.HealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for health check endpoints
 * 健康檢查端點的回應 DTO
 */
@Schema(description = "健康檢查回應物件")
public record HealthResponse(
    @Schema(description = "服務狀態", example = "UP") String status,
    @Schema(description = "服務名稱", example = "demo-app") String service) {

    public static HealthResponse fromDomain(HealthStatus healthStatus) {
        return new HealthResponse(
                healthStatus.getStatus().name(),
                healthStatus.getServiceName()
        );
    }
}