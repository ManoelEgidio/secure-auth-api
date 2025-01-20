package com.secure.auth_service.controllers;

import com.secure.auth_service.dtos.AuthenticationDTO;
import com.secure.auth_service.dtos.UserRegisterDTO;
import com.secure.auth_service.dtos.UserSummaryDTO;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.services.UserService;
import com.secure.auth_service.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Operações relacionadas a autenticação")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;

    @Operation(summary = "Fazer login")
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid AuthenticationDTO data, HttpServletResponse response) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());
            Authentication auth = this.authenticationManager.authenticate(authToken);

            var user = (User) auth.getPrincipal();

            var accessToken = tokenUtils.generateAccessToken(user);
            var refreshToken = tokenUtils.generateRefreshToken(user);
            var idToken = tokenUtils.generateIdToken(user);

            long accessTokenMaxAge = tokenUtils.getAccessTokenMaxAge();
            long refreshTokenMaxAge = tokenUtils.getRefreshTokenMaxAge();
            long idTokenMaxAge = tokenUtils.getIdTokenMaxAge();

            ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(accessTokenMaxAge)
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(refreshTokenMaxAge)
                    .build();

            ResponseCookie idCookie = ResponseCookie.from("id_token", idToken)
                    .httpOnly(false)
                    .secure(true)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(idTokenMaxAge)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, idCookie.toString());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new CustomException("Falha no login: " + e.getMessage());
        }
    }

    @Operation(summary = "Fazer logout")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        ResponseCookie idCookie = ResponseCookie.from("id_token", "")
                .httpOnly(false)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, idCookie.toString());

        return ResponseEntity.ok().build();
    }
}
