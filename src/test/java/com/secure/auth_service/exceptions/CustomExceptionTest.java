package com.secure.auth_service.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionTest {

    @Test
    void customException_CreatesCorrectMessage() {
        CustomException exception = new CustomException("Test message");
        assertEquals("Test message", exception.getMessage());
    }
}
