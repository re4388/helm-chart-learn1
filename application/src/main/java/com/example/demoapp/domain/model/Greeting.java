package com.example.demoapp.domain.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing a greeting message
 * 領域實體：問候訊息
 */
public class Greeting {
    private final String message;
    private final LocalDateTime timestamp;
    private final String recipient;

    public Greeting(String message, String recipient) {
        this.message = message;
        this.recipient = recipient;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getFormattedMessage() {
        if (recipient != null && !recipient.isEmpty()) {
            return "Hello, " + recipient + "!";
        }
        return "Hello from Demo Spring Boot App!";
    }
}