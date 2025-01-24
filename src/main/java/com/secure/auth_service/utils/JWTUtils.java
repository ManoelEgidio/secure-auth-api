package com.secure.auth_service.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JWTUtils {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public String generateRefreshToken(User user, Instant expiration) {
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(user.getLogin())
                .withClaim("type", "refresh")
                .withExpiresAt(expiration)
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    public String generateAccessToken(User user, Instant expiration) {
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(user.getLogin())
                .withClaim("id", user.getId().toString())
                .withClaim("role", user.getRole().name())
                .withArrayClaim("authorities", extractCleanAuthorities(user))
                .withExpiresAt(expiration)
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    public String generateIdToken(User user, Instant expiration) {
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(user.getLogin())
                .withClaim("id", user.getId().toString())
                .withClaim("name", user.getName())
                .withClaim("role", user.getRole().name())
                .withArrayClaim("authorities", extractCleanAuthorities(user))
                .withExpiresAt(expiration)
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    private String[] extractCleanAuthorities(User user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .toArray(String[]::new);
    }

    public Set<Authority> extractAuthorities(DecodedJWT jwt) {
        String[] authorities = jwt.getClaim("authorities").asArray(String.class);
        return authorities != null ?
                Arrays.stream(authorities)
                        .map(Authority::valueOf)
                        .collect(Collectors.toSet()) :
                Collections.emptySet();
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        return JWT.require(Algorithm.RSA256(publicKey, privateKey))
                .withIssuer("auth-api")
                .build()
                .verify(token);
    }

    public User extractUserFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return buildUserFromJWT(jwt);
    }

    private User buildUserFromJWT(DecodedJWT jwt) {
        User user = new User();
        user.setId(UUID.fromString(jwt.getClaim("id").asString()));
        user.setLogin(jwt.getSubject());
        user.setRole(Roles.valueOf(jwt.getClaim("role").asString()));
        user.setAuthorities(extractAuthorities(jwt));
        user.setName(jwt.getClaim("name").asString());
        return user;
    }

    public Instant genAccessTokenExpiration(long minutes) {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .plusMinutes(minutes)
                .toInstant();
    }

    public Instant genRefreshTokenExpiration(long days) {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .plusDays(days)
                .toInstant();
    }
}