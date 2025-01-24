package com.secure.auth_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${redis.enabled:true}")
    private boolean redisEnabled;

    public boolean isRedisAvailable() {
        return redisEnabled && redisTemplate != null;
    }

    public void addUserToken(String userId, String token, long ttlSeconds) {
        if (!isRedisAvailable()) return;

        String key = "user_tokens:" + userId;
        redisTemplate.opsForSet().add(key, token);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    public void addToBlacklist(String token, long ttlSeconds) {
        if (!isRedisAvailable()) return;

        redisTemplate.opsForValue().set("blacklist:" + token, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        if (!isRedisAvailable()) return false;

        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    public void invalidateAllUserTokens(String userId) {
        if (!isRedisAvailable()) return;

        String key = "user_tokens:" + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        if (tokens != null) {
            tokens.forEach(token -> addToBlacklist(token, 60));
        }
        redisTemplate.delete(key);
    }

    public void addToRefreshWhitelist(String userId, String refreshToken, long ttlSeconds) {
        if (!isRedisAvailable()) return;

        String key = "refresh_whitelist:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isRefreshTokenValid(String userId, String refreshToken) {
        if (!isRedisAvailable()) return true;

        String storedToken = redisTemplate.opsForValue().get("refresh_whitelist:" + userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void invalidateRefreshToken(String userId) {
        if (!isRedisAvailable()) return;

        redisTemplate.delete("refresh_whitelist:" + userId);
    }
}