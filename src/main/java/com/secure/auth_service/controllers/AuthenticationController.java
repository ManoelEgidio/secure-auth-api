package com.secure.auth_service.controllers;

import com.secure.auth_service.dtos.AuthenticationDTO;
import com.secure.auth_service.models.User;
import com.secure.auth_service.services.RedisService;
import com.secure.auth_service.utils.AuthenticationUtils;
import com.secure.auth_service.utils.CookieUtils;
import com.secure.auth_service.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação",
        description = "Gerencia o ciclo de vida das sessões de usuário, incluindo login, logout e renovação de tokens")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationUtils authUtils;
    private final CookieUtils cookieUtils;
    private final RedisService redisService;
    private final TokenUtils tokenUtils;

    @Operation(summary = "Autenticar usuário",
            description = "Realiza o login do usuário e retorna os tokens de acesso e refresh")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestBody @Valid AuthenticationDTO data, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);

        User user = (User) auth.getPrincipal();
        authUtils.refreshAllTokens(user, response);
    }

    @Operation(summary = "Renovar tokens",
            description = "Gera novos tokens de acesso e refresh utilizando o refresh token atual")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        boolean success = authUtils.tryAutoLogin(request, response);
        if (!success) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido ou expirado");
        }
    }

    @Operation(summary = "Encerrar sessão",
            description = "Realiza o logout do usuário no dispositivo atual, invalidando o token de acesso")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal User user) {
        String accessToken = cookieUtils.getTokenFromCookie(request, "access_token");

        if (accessToken != null) {
            redisService.addToBlacklist(accessToken, tokenUtils.getAccessTokenMaxAge());
        }

        cookieUtils.clearAuthCookies(response);
    }

    @Operation(summary = "Encerrar todas as sessões",
            description = "Realiza o logout do usuário em todos os dispositivos. " +
                    "Requer Redis ativo para funcionar corretamente.")
    @PostMapping("/logout/all")
    @ResponseStatus(HttpStatus.OK)
    public void logoutEverywhere(@AuthenticationPrincipal User user, HttpServletResponse response) {
        if (!redisService.isRedisAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Funcionalidade indisponível: Redis não está ativo"
            );
        }

        redisService.invalidateAllUserTokens(user.getId().toString());
        cookieUtils.clearAuthCookies(response);
    }
}