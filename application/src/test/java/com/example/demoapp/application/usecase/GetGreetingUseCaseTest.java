package com.example.demoapp.application.usecase;

import com.example.demoapp.domain.model.Greeting;
import com.example.demoapp.domain.port.GreetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetGreetingUseCaseTest {

    @Mock
    private GreetingService greetingService;

    private GetGreetingUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetGreetingUseCase(greetingService);
    }

    @Test
    void shouldExecuteGeneralGreeting() {
        // Given
        Greeting expectedGreeting = new Greeting("Hello!", null);
        when(greetingService.createGeneralGreeting()).thenReturn(expectedGreeting);

        // When
        Greeting result = useCase.execute();

        // Then
        assertEquals(expectedGreeting, result);
        verify(greetingService).createGeneralGreeting();
    }

    @Test
    void shouldExecutePersonalizedGreeting() {
        // Given
        String name = "John";
        Greeting expectedGreeting = new Greeting("Hello, John!", name);
        when(greetingService.createPersonalizedGreeting(name)).thenReturn(expectedGreeting);

        // When
        Greeting result = useCase.execute(name);

        // Then
        assertEquals(expectedGreeting, result);
        verify(greetingService).createPersonalizedGreeting(name);
    }

    @Test
    void shouldThrowExceptionForNullName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(""));
    }
}