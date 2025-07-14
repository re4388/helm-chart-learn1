package com.example.demoapp.adapter.web.controller;

import com.example.demoapp.adapter.web.dto.GreetingResponse;
import com.example.demoapp.adapter.web.dto.HealthResponse;
import com.example.demoapp.application.usecase.GetGreetingUseCase;
import com.example.demoapp.application.usecase.GetHealthStatusUseCase;
import com.example.demoapp.domain.model.Greeting;
import com.example.demoapp.domain.model.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller following hexagonal architecture principles
 */
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

    @GetMapping("/")
    public GreetingResponse home() {
        Greeting greeting = getGreetingUseCase.execute();
        return GreetingResponse.fromDomain(greeting, version);
    }

    @GetMapping("/hello/{name}")
    public GreetingResponse hello(@PathVariable String name) {
        Greeting greeting = getGreetingUseCase.execute(name);
        return GreetingResponse.fromDomain(greeting, version);
    }

    @GetMapping("/health")
    public HealthResponse health() {
        HealthStatus healthStatus = getHealthStatusUseCase.execute();
        return HealthResponse.fromDomain(healthStatus);
    }
}