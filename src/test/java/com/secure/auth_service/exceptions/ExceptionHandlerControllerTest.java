package com.secure.auth_service.exceptions;

import com.secure.auth_service.dtos.StandardErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionHandlerControllerTest {

    @InjectMocks
    private ExceptionHandlerController exceptionHandlerController;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/test-endpoint");
    }

    @Test
    void authenticationException_ReturnsStandardErrorDTO() {
        AuthenticationException authException = mock(AuthenticationException.class);
        when(authException.getMessage()).thenReturn("Invalid credentials");

        StandardErrorDTO response = exceptionHandlerController.authenticationException(authException, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), response.getError());
        assertEquals("Usuário não encontrado ou senha inválida!", response.getMessage());
        assertEquals("/test-endpoint", response.getPath());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void methodArgumentNotValidException_ReturnsListOfStandardErrorDTO() {
        MethodArgumentNotValidException methodArgException = mock(MethodArgumentNotValidException.class);
        var fieldError1 = mock(org.springframework.validation.FieldError.class);
        var fieldError2 = mock(org.springframework.validation.FieldError.class);

        when(methodArgException.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(methodArgException.getBindingResult().getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(fieldError1.getDefaultMessage()).thenReturn("Field1 is invalid");
        when(fieldError2.getDefaultMessage()).thenReturn("Field2 is invalid");

        List<StandardErrorDTO> responses = exceptionHandlerController.methodArgumentNotValidException(methodArgException, request);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        StandardErrorDTO response1 = responses.get(0);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response1.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response1.getError());
        assertEquals("Field1 is invalid", response1.getMessage());
        assertEquals("/test-endpoint", response1.getPath());
        assertTrue(response1.getTimestamp() > 0);

        StandardErrorDTO response2 = responses.get(1);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response2.getError());
        assertEquals("Field2 is invalid", response2.getMessage());
        assertEquals("/test-endpoint", response2.getPath());
        assertTrue(response2.getTimestamp() > 0);
    }

    @Test
    void customException_ReturnsStandardErrorDTO() {
        CustomException customException = new CustomException("Custom error occurred");

        StandardErrorDTO response = exceptionHandlerController.customException(customException, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getError());
        assertEquals("Custom error occurred", response.getMessage());
        assertEquals("/test-endpoint", response.getPath());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void dataIntegrityViolationException_ReturnsStandardErrorDTO() {
        DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("Integrity violation");

        StandardErrorDTO response = exceptionHandlerController.dataIntegrityViolationException(dataIntegrityException, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), response.getError());
        assertEquals("Não foi possível concluir a operação devido a restrições de integridade de dados.", response.getMessage());
        assertEquals("/test-endpoint", response.getPath());
        assertTrue(response.getTimestamp() > 0);
    }
}
