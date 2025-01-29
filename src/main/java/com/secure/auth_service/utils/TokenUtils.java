package com.secure.auth_service.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.models.User;
import com.secure.auth_service.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenUtils {

    @Value("${jwt.access.token.expiration.minutes}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh.token.expiration.days}")
    private long refreshTokenExpirationDays;

    @Value("${jwt.activation.token.expiration.minutes:60}") // 1 hora padrão
    private long activationTokenExpirationMinutes;

    @Value("${jwt.recovery.token.expiration.minutes:30}") // 30 minutos padrão
    private long recoveryTokenExpirationMinutes;

    private final JWTUtils jwtUtils;
    private final RedisService redisService;

    public String generateAccessToken(User user) {
        return jwtUtils.generateAccessToken(user,
                jwtUtils.genAccessTokenExpiration(getAccessTokenMaxAge()));
    }

    public String generateRefreshToken(User user) {
        return jwtUtils.generateRefreshToken(user,
                jwtUtils.genRefreshTokenExpiration(getRefreshTokenMaxAge()));
    }

    public String generateIdToken(User user) {
        return jwtUtils.generateIdToken(user,
                jwtUtils.genRefreshTokenExpiration(getRefreshTokenMaxAge()));
    }

    public boolean validateAccessToken(String token) {
        if (redisService.isTokenBlacklisted(token)) {
            return false;
        }

        try {
            jwtUtils.verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken, String userId) {
        try {
            jwtUtils.verifyToken(refreshToken);
            return redisService.isRefreshTokenValid(userId, refreshToken);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean validateActivationToken(String token) {
        try {
            jwtUtils.verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean validateRecoveryToken(String token) {
        try {
            jwtUtils.verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public User getUserFromIdToken(String idToken) {
        return jwtUtils.extractUserFromToken(idToken);
    }

    public long getAccessTokenMaxAge() {
        return accessTokenExpirationMinutes * 60;
    }

    public long getRefreshTokenMaxAge() {
        return refreshTokenExpirationDays * 24 * 60 * 60;
    }

    public String getLoginFromToken(String token) {
        try {
            return jwtUtils.verifyToken(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            return jwtUtils.verifyToken(token).getClaim("role").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getIdFromToken(String token) {
        try {
            return jwtUtils.verifyToken(token).getClaim("id").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public Set<Authority> getAuthoritiesFromToken(String token) {
        try {
            return jwtUtils.extractAuthorities(jwtUtils.verifyToken(token));
        } catch (JWTVerificationException e) {
            return Collections.emptySet();
        }
    }

    /**
     * Gera um token JWT específico para ativação de conta.
     *
     * @param user Usuário para quem o token será gerado
     * @return Token JWT de ativação
     */
    public String generateActivationToken(User user) {
        return jwtUtils.generateActivationToken(user,
                jwtUtils.genActivationTokenExpiration(activationTokenExpirationMinutes));
    }

    /**
     * Gera um token JWT específico para recuperação de senha.
     *
     * @param user Usuário para quem o token será gerado
     * @return Token JWT de recuperação
     */
    public String generateRecoveryToken(User user) {
        return jwtUtils.generateRecoveryToken(user,
                jwtUtils.genRecoveryTokenExpiration(recoveryTokenExpirationMinutes));
    }
}