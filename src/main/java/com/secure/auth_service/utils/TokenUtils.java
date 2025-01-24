package com.secure.auth_service.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.models.User;
import com.secure.auth_service.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenUtils {

    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 30;
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    private final JWTUtils jwtUtils;
    private final RedisService redisService;

    public String generateAccessToken(User user) {
        return jwtUtils.generateAccessToken(user,
                jwtUtils.genAccessTokenExpiration(ACCESS_TOKEN_EXPIRATION_MINUTES));
    }

    public String generateRefreshToken(User user) {
        return jwtUtils.generateRefreshToken(user,
                jwtUtils.genRefreshTokenExpiration(REFRESH_TOKEN_EXPIRATION_DAYS));
    }

    public String generateIdToken(User user) {
        return jwtUtils.generateIdToken(user,
                jwtUtils.genRefreshTokenExpiration(REFRESH_TOKEN_EXPIRATION_DAYS));
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

    public User getUserFromIdToken(String idToken) {
        return jwtUtils.extractUserFromToken(idToken);
    }

    public long getAccessTokenMaxAge() {
        return ACCESS_TOKEN_EXPIRATION_MINUTES * 60;
    }

    public long getRefreshTokenMaxAge() {
        return REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60;
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

    public Set<Authority> getAuthoritiesFromToken(String token) {
        try {
            return jwtUtils.extractAuthorities(jwtUtils.verifyToken(token));
        } catch (JWTVerificationException e) {
            return Collections.emptySet();
        }
    }
}