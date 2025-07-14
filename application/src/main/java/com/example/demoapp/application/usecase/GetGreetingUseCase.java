package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.Greeting;
import com.example.demoapp.domain.port.GreetingService;
import org.springframework.stereotype.Service;

/**
 * Use case for getting greeting messages
 * 用例：取得問候訊息
 */
@Service
public class GetGreetingUseCase {
    
    private final GreetingService greetingService;
    
    public GetGreetingUseCase(GreetingService greetingService) {
        this.greetingService = greetingService;
    }
    
    /**
     * Execute use case to get general greeting
     * 執行用例以取得一般問候
     */
    public Greeting execute() {
        return greetingService.createGeneralGreeting();
    }
    
    /**
     * Execute use case to get personalized greeting
     * 執行用例以取得個人化問候
     */
    public Greeting execute(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return greetingService.createPersonalizedGreeting(name.trim());
    }
}