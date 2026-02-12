package com.example.demoapp.domain.dto;

import com.example.demoapp.domain.model.Greeting;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response DTO for greeting endpoints
 * 問候端點的回應 DTO
 */
@Schema(description = "問候回應物件")
public record GreetingResponse(
    @Schema(description = "問候訊息", example = "Hello, World! from demo-app") String message,
    @Schema(description = "時間戳記", example = "2023-10-26T10:00:00") LocalDateTime timestamp,
    @Schema(description = "應用程式版本", example = "1.0.0") String version) {

    public static GreetingResponse fromDomain(Greeting greeting, String version) {
        return new GreetingResponse(
                greeting.getFormattedMessage(),
                greeting.getTimestamp(),
                version
        );
    }
}