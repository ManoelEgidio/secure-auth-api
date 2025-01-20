package com.secure.auth_service.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import com.secure.auth_service.dtos.StandardErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public StandardErrorDTO authenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.error("Falha na autenticação! {}", ex.getMessage());
        return buildStandardError(HttpStatus.UNAUTHORIZED, "Usuário não encontrado ou senha inválida!", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<StandardErrorDTO> methodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Falha no argumento do método inválido {}", ex.getMessage());
        return ex.getBindingResult().getFieldErrors().stream()
                .map(error -> buildStandardError(HttpStatus.BAD_REQUEST, error.getDefaultMessage(), request))
                .collect(Collectors.toList());
    }

    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public StandardErrorDTO customException(CustomException ex, HttpServletRequest request) {
        log.error("Erro personalizado: {}", ex.getMessage());
        return buildStandardError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public StandardErrorDTO dataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Violação de integridade de dados: {}", ex.getMessage());
        String message = "Não foi possível concluir a operação devido a restrições de integridade de dados.";
        return buildStandardError(HttpStatus.CONFLICT, message, request);
    }

    private StandardErrorDTO buildStandardError(HttpStatus status, String message, HttpServletRequest request) {
        return new StandardErrorDTO(System.currentTimeMillis(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
    }
}
