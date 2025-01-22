package com.secure.auth_service.controllers;

import com.secure.auth_service.dtos.AuthenticationDTO;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.utils.TokenUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenUtils tokenUtils;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Test
    void login_Success() {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setLogin("joao.silva@example.com");
        authDTO.setPassword("Senha123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .login(authDTO.getLogin())
                .password("Senha123")
                .role(Roles.USER)
                .authorities(Set.of(Authority.CREATE))
                .enabled(true)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        when(tokenUtils.generateAccessToken(user)).thenReturn("access-token");
        when(tokenUtils.generateRefreshToken(user)).thenReturn("refresh-token");
        when(tokenUtils.generateIdToken(user)).thenReturn("id-token");
        when(tokenUtils.getAccessTokenMaxAge()).thenReturn(3600L);
        when(tokenUtils.getRefreshTokenMaxAge()).thenReturn(7200L);
        when(tokenUtils.getRefreshTokenMaxAge()).thenReturn(7200L);

        ResponseEntity<Void> response = authenticationController.login(authDTO, httpServletResponse);

        assertEquals(ResponseEntity.ok().build(), response);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenUtils, times(1)).generateAccessToken(user);
        verify(tokenUtils, times(1)).generateRefreshToken(user);
        verify(tokenUtils, times(1)).generateIdToken(user);
        verify(httpServletResponse, times(3)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void login_Failure() {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setLogin("joao.silva@example.com");
        authDTO.setPassword("Senha123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        CustomException exception = assertThrows(CustomException.class, () -> authenticationController.login(authDTO, httpServletResponse));
        assertTrue(exception.getMessage().contains("Falha no login:"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenUtils, never()).generateAccessToken(any());
        verify(tokenUtils, never()).generateRefreshToken(any());
        verify(tokenUtils, never()).generateIdToken(any());
        verify(httpServletResponse, never()).addHeader(anyString(), anyString());
    }

    @Test
    void logout_Success() {
        ResponseEntity<Void> response = authenticationController.logout(httpServletResponse);

        assertEquals(ResponseEntity.ok().build(), response);

        verify(httpServletResponse, times(3)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }
}
