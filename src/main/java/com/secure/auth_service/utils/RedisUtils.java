package com.secure.auth_service.utils;

import com.secure.auth_service.enums.TokenType;
import com.secure.auth_service.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisService redisService;

    /**
     * Armazena um token no Redis com um tipo específico e TTL.
     *
     * @param tokenType   Tipo do token (ACTIVATION, RECOVERY)
     * @param token       O token a ser armazenado
     * @param userId      ID do usuário associado
     * @param ttlSeconds Tempo de vida do token em segundos
     */
    public void storeToken(TokenType tokenType, String token, String userId, long ttlSeconds) {
        String key = generateKey(tokenType, token);
        redisService.putValue(key, userId, ttlSeconds);
    }

    /**
     * Recupera o ID do usuário associado a um token.
     *
     * @param tokenType Tipo do token
     * @param token     O token a ser verificado
     * @return ID do usuário ou null se o token não for encontrado
     */
    public String getUserIdByToken(TokenType tokenType, String token) {
        String key = generateKey(tokenType, token);
        return redisService.getValue(key);
    }

    /**
     * Remove um token do Redis.
     *
     * @param tokenType Tipo do token
     * @param token     O token a ser removido
     */
    public void removeToken(TokenType tokenType, String token) {
        String key = generateKey(tokenType, token);
        redisService.deleteKey(key);
    }

    /**
     * Gera a chave no Redis para um token específico.
     *
     * @param tokenType Tipo do token
     * @param token     O token
     * @return Chave formatada
     */
    private String generateKey(TokenType tokenType, String token) {
        return tokenType.name().toLowerCase() + "_token:" + token;
    }
}
