package com.example.demoapp.domain.model;

/**
 * Domain entity representing application health status
 * 領域實體：應用程式健康狀態
 */
public class HealthStatus {
    private final Status status;
    private final String serviceName;

    public enum Status {
        UP, DOWN, UNKNOWN
    }

    public HealthStatus(Status status, String serviceName) {
        this.status = status;
        this.serviceName = serviceName;
    }

    public Status getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isHealthy() {
        return status == Status.UP;
    }
}