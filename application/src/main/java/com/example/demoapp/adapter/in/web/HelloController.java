package com.example.demoapp.adapter.in;

import com.example.demoapp.domain.dto.GreetingResponseDTO;
import com.example.demoapp.domain.dto.HealthResponseDTO;
import com.example.demoapp.application.usecase.GetGreetingUseCase;
import com.example.demoapp.application.usecase.GetHealthStatusUseCase;
import com.example.demoapp.domain.model.Greeting;
import com.example.demoapp.domain.model.HealthStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller following hexagonal architecture principles
 */
@Tag(name = "問候與健康檢查", description = "提供應用程式的基本問候語和健康檢查功能")
@RestController
public class HelloController {

    private final GetGreetingUseCase getGreetingUseCase;
    private final GetHealthStatusUseCase getHealthStatusUseCase;
    private final String version;

    public HelloController(GetGreetingUseCase getGreetingUseCase, 
                          GetHealthStatusUseCase getHealthStatusUseCase,
                          @Value("${app.version:1.0.0}") String version) {
        this.getGreetingUseCase = getGreetingUseCase;
        this.getHealthStatusUseCase = getHealthStatusUseCase;
        this.version = version;
    }

    @Operation(summary = "獲取預設問候語", description = "返回應用程式的預設問候語和版本資訊。")
    @ApiResponse(responseCode = "200", description = "成功獲取問候語",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GreetingResponseDTO.class)))
    @GetMapping("/")
    public GreetingResponseDTO home() {
        Greeting greeting = getGreetingUseCase.execute();
        return GreetingResponseDTO.fromDomain(greeting, version);
    }

    @Operation(summary = "獲取個性化問候語", description = "根據提供的名稱返回個性化問候語和版本資訊。")
    @ApiResponse(responseCode = "200", description = "成功獲取個性化問候語",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GreetingResponseDTO.class)))
    @GetMapping("/hello/{name}")
    public GreetingResponseDTO hello(@Parameter(description = "要問候的名稱", required = true, example = "World") @PathVariable String name) {
        Greeting greeting = getGreetingUseCase.execute(name);
        return GreetingResponseDTO.fromDomain(greeting, version);
    }

    @Operation(summary = "獲取應用程式健康狀態", description = "返回應用程式的當前健康狀態。")
    @ApiResponse(responseCode = "200", description = "成功獲取健康狀態",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthResponseDTO.class)))
    @GetMapping("/health")
    public HealthResponseDTO health() {
        HealthStatus healthStatus = getHealthStatusUseCase.execute();
        return HealthResponseDTO.fromDomain(healthStatus);
    }
}