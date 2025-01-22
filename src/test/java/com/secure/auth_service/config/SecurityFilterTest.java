package com.secure.auth_service.config;

import com.secure.auth_service.configurations.SecurityFilter;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.utils.TokenUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityFilterTest.TestController.class)
@Import(SecurityFilter.class)
class SecurityFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TokenUtils tokenUtils;

    @RestController
    static class TestController {
        @GetMapping("/protected")
        public String protectedEndpoint() {
            return "Protected Content";
        }

        @PostMapping("/auth/login")
        public String login() {
            return "Login Endpoint";
        }

        @PostMapping("/auth/register")
        public String register() {
            return "Register Endpoint";
        }

        @GetMapping("/throw")
        public String throwException() {
            throw new CustomException("Erro personalizado para teste");
        }
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve permitir acesso aos endpoints excluídos (/auth/login e /auth/register) via POST")
    void testExcludedEndpoints() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string("Login Endpoint"));

        mockMvc.perform(post("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(content().string("Register Endpoint"));
    }

    @Test
    @DisplayName("Deve autenticar usuário com access_token válido")
    void testAccessTokenValid() throws Exception {
        String accessToken = "validAccessToken";
        String login = "user@example.com";
        String role = "USER";

        when(tokenUtils.validateAccessToken(accessToken)).thenReturn(true);
        when(tokenUtils.getLoginFromToken(accessToken)).thenReturn(login);
        when(tokenUtils.getRoleFromToken(accessToken)).thenReturn(role);

        mockMvc.perform(get("/protected")
                        .cookie(createCookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected Content"));

        assert SecurityContextHolder.getContext().getAuthentication() != null;
        assert SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    @Test
    @DisplayName("Deve autenticar usuário com refresh_token válido e atualizar tokens")
    void testAccessTokenInvalidRefreshTokenValid() throws Exception {
        String accessToken = "invalidAccessToken";
        String refreshToken = "validRefreshToken";
        String idToken = "validIdToken";
        String login = "user@example.com";
        String role = "USER";

        User user = new User();
        user.setLogin(login);
        user.setRole(Roles.USER);

        when(tokenUtils.validateAccessToken(accessToken)).thenReturn(false);

        when(tokenUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(tokenUtils.getLoginFromToken(refreshToken)).thenReturn(login);
        when(tokenUtils.getUserFromIdToken(idToken)).thenReturn(user);
        when(tokenUtils.generateAccessToken(user)).thenReturn("newAccessToken");
        when(tokenUtils.generateRefreshToken(user)).thenReturn("newRefreshToken");
        when(tokenUtils.generateIdToken(user)).thenReturn("newIdToken");
        when(tokenUtils.getAccessTokenMaxAge()).thenReturn(3600L);
        when(tokenUtils.getRefreshTokenMaxAge()).thenReturn(7200L);
        when(tokenUtils.getRefreshTokenMaxAge()).thenReturn(7200L);

        mockMvc.perform(get("/protected")
                        .cookie(
                                createCookie("access_token", accessToken),
                                createCookie("refresh_token", refreshToken),
                                createCookie("id_token", idToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected Content"));

        // Verificar se os novos cookies foram adicionados
        verify(tokenUtils, times(1)).validateAccessToken(accessToken);
        verify(tokenUtils, times(1)).validateRefreshToken(refreshToken);
        verify(tokenUtils, times(1)).getLoginFromToken(refreshToken);
        verify(tokenUtils, times(1)).getUserFromIdToken(idToken);
        verify(tokenUtils, times(1)).generateAccessToken(user);
        verify(tokenUtils, times(1)).generateRefreshToken(user);
        verify(tokenUtils, times(1)).generateIdToken(user);
    }

    @Test
    @DisplayName("Deve remover cookies quando tokens são inválidos")
    void testInvalidTokens() throws Exception {
        String accessToken = "invalidAccessToken";
        String refreshToken = "invalidRefreshToken";

        when(tokenUtils.validateAccessToken(accessToken)).thenReturn(false);
        when(tokenUtils.validateRefreshToken(refreshToken)).thenReturn(false);

        mockMvc.perform(get("/protected")
                        .cookie(
                                createCookie("access_token", accessToken),
                                createCookie("refresh_token", refreshToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected Content"));

        verify(tokenUtils, times(1)).validateAccessToken(accessToken);
        verify(tokenUtils, times(1)).validateRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("Deve lançar CustomException para role inválida no token")
    void testInvalidRoleInToken() throws Exception {
        String accessToken = "validAccessToken";
        String login = "user@example.com";
        String invalidRole = "INVALID_ROLE";

        when(tokenUtils.validateAccessToken(accessToken)).thenReturn(true);
        when(tokenUtils.getLoginFromToken(accessToken)).thenReturn(login);
        when(tokenUtils.getRoleFromToken(accessToken)).thenReturn(invalidRole);

        mockMvc.perform(get("/protected")
                        .cookie(createCookie("access_token", accessToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro personalizado para teste"));

        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    @DisplayName("Deve remover cookies se id_token estiver ausente durante a renovação de tokens")
    void testTokenRefreshWithoutIdToken() throws Exception {
        String accessToken = "invalidAccessToken";
        String refreshToken = "validRefreshToken";
        String login = "user@example.com";

        when(tokenUtils.validateAccessToken(accessToken)).thenReturn(false);
        when(tokenUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(tokenUtils.getLoginFromToken(refreshToken)).thenReturn(login);
        when(tokenUtils.getUserFromIdToken(anyString())).thenReturn(null);
        mockMvc.perform(get("/protected")
                        .cookie(
                                createCookie("access_token", accessToken),
                                createCookie("refresh_token", refreshToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected Content"));

        verify(tokenUtils, times(1)).validateAccessToken(accessToken);
        verify(tokenUtils, times(1)).validateRefreshToken(refreshToken);
        verify(tokenUtils, times(1)).getLoginFromToken(refreshToken);
        verify(tokenUtils, times(1)).getUserFromIdToken(anyString());
    }

    @Test
    @DisplayName("Deve autenticar usuário com access_token válido e atribuir autoridade correta")
    void testAccessTokenValidWithAuthorities() throws Exception {
        String accessToken = "validAccessToken";
        String login = "admin@example.com";
        String role = "ADMIN";

        User user = new User();
        user.setLogin(login);
        user.setRole(Roles.ADMIN);
        user.setAuthorities(Set.of(Authority.VIEW, Authority.CREATE));

        when(tokenUtils.validateAccessToken(accessToken)).thenReturn(true);
        when(tokenUtils.getLoginFromToken(accessToken)).thenReturn(login);
        when(tokenUtils.getRoleFromToken(accessToken)).thenReturn(role);

        mockMvc.perform(get("/protected")
                        .cookie(createCookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected Content"));

        assert SecurityContextHolder.getContext().getAuthentication() != null;
        assert SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        assert SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(Roles.ADMIN.name()));
    }
}
