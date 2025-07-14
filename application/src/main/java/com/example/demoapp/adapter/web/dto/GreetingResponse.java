package com.example.demoapp.adapter.web.dto;

import com.example.demoapp.domain.model.Greeting;
import java.time.LocalDateTime;

/**
 * Response DTO for greeting endpoints
 * 問候端點的回應 DTO
 */
public record GreetingResponse(String message, LocalDateTime timestamp, String version) {

    public static GreetingResponse fromDomain(Greeting greeting, String version) {
        return new GreetingResponse(
                greeting.getFormattedMessage(),
                greeting.getTimestamp(),
                version
        );
    }
}