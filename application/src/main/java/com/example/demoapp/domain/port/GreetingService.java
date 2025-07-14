package com.example.demoapp.domain.port;

import com.example.demoapp.domain.model.Greeting;

/**
 * Domain service interface for greeting operations
 * 領域服務介面：問候操作
 */
public interface GreetingService {
    
    /**
     * Create a general greeting
     * 建立一般問候
     */
    Greeting createGeneralGreeting();
    
    /**
     * Create a personalized greeting for a specific recipient
     * 為特定接收者建立個人化問候
     */
    Greeting createPersonalizedGreeting(String recipient);
}