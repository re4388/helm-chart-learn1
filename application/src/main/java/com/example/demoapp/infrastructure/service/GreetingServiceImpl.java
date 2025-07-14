package com.example.demoapp.infrastructure.service;

import com.example.demoapp.domain.model.Greeting;
import com.example.demoapp.domain.port.GreetingService;
import org.springframework.stereotype.Service;

/**
 * Implementation of GreetingService
 * GreetingService 的實作
 */
@Service
public class GreetingServiceImpl implements GreetingService {
    
    private static final String DEFAULT_MESSAGE = "Hello from Demo Spring Boot App!";
    
    @Override
    public Greeting createGeneralGreeting() {
        return new Greeting(DEFAULT_MESSAGE, null);
    }
    
    @Override
    public Greeting createPersonalizedGreeting(String recipient) {
        String personalizedMessage = "Hello, " + recipient + "!";
        return new Greeting(personalizedMessage, recipient);
    }
}