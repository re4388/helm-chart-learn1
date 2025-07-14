package com.example.demoapp.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreetingTest {

    @Test
    void shouldCreateGeneralGreeting() {
        // Given
        String message = "Hello from Demo Spring Boot App!";
        
        // When
        Greeting greeting = new Greeting(message, null);
        
        // Then
        assertEquals(message, greeting.getMessage());
        assertEquals("Hello from Demo Spring Boot App!", greeting.getFormattedMessage());
        assertNotNull(greeting.getTimestamp());
    }

    @Test
    void shouldCreatePersonalizedGreeting() {
        // Given
        String message = "Hello, John!";
        String recipient = "John";
        
        // When
        Greeting greeting = new Greeting(message, recipient);
        
        // Then
        assertEquals(message, greeting.getMessage());
        assertEquals("Hello, John!", greeting.getFormattedMessage());
        assertEquals(recipient, greeting.getRecipient());
        assertNotNull(greeting.getTimestamp());
    }
}