package com.secure.auth_service.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.models.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TokenUtils {

    @Value("${jwt.private-key}")
    String privateKeyPem;

    @Value("${jwt.public-key}")
    String publicKeyPem;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 30;
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(privateKeyPem);
            this.publicKey = loadPublicKey(publicKeyPem);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar as chaves", e);
        }
    }

    private RSAPrivateKey loadPrivateKey(String key) {
        try {
            String privateKeyContent = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar a chave privada: " + e.getMessage(), e);
        }
    }

    private RSAPublicKey loadPublicKey(String key) {
        try {
            String publicKeyContent = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\n", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar a chave pública: " + e.getMessage(), e);
        }
    }

    public String generateRefreshToken(User user) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getLogin())
                    .withClaim("type", "refresh")
                    .withExpiresAt(genRefreshTokenExpiration())
                    .sign(algorithm);
            System.out.println("Refresh Token gerado com sucesso.");
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar o Refresh Token: " + e.getMessage(), e);
        }
    }

    public String generateAccessToken(User user) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getLogin())
                    .withClaim("id", user.getId() != null ? user.getId().toString() : null)
                    .withClaim("role", user.getRole().name())
                    .withArrayClaim("authorities",
                            user.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .toArray(String[]::new))
                    .withExpiresAt(genAccessTokenExpiration())
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar o Access Token: " + e.getMessage(), e);
        }
    }

    public String generateIdToken(User user) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getLogin())
                    .withClaim("id", user.getId() != null ? user.getId().toString() : null)
                    .withClaim("name", user.getName())
                    .withClaim("role", user.getRole().name())
                    .withArrayClaim("authorities",
                            user.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .toArray(String[]::new))
                    .withExpiresAt(genRefreshTokenExpiration())
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar o ID Token: " + e.getMessage(), e);
        }
    }

    public String getLoginFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
            return jwt.getClaim("role").asString();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public Set<Authority> getAuthoritiesFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);

            String[] authoritiesArray = jwt.getClaim("authorities").asArray(String.class);
            if (authoritiesArray != null) {
                return Arrays.stream(authoritiesArray)
                        .map(Authority::valueOf)
                        .collect(Collectors.toSet());
            } else {
                return Collections.emptySet();
            }
        } catch (JWTVerificationException exception) {
            return Collections.emptySet();
        }
    }

    public User getUserFromIdToken(String idToken) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(idToken);

            User user = new User();

            String idStr = jwt.getClaim("id").asString();
            if (idStr != null && !idStr.isEmpty()) {
                user.setId(UUID.fromString(idStr));
            }

            user.setLogin(jwt.getSubject());

            String roleStr = jwt.getClaim("role").asString();
            user.setRole(Roles.valueOf(roleStr));

            String[] authoritiesArray = jwt.getClaim("authorities").asArray(String.class);
            if (authoritiesArray != null) {
                Set<Authority> authorities = new HashSet<>();
                for (String authority : authoritiesArray) {
                    if (!authority.startsWith("ROLE_")) {
                        authorities.add(Authority.valueOf(authority));
                    }
                }
                user.setAuthorities(authorities);
            }

            user.setName(jwt.getClaim("name").asString());

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao reconstruir usuário do ID token: " + e.getMessage(), e);
        }
    }




    public boolean validateAccessToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .withClaim("type", "refresh")
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public long getAccessTokenMaxAge() {
        return ACCESS_TOKEN_EXPIRATION_MINUTES * 30;
    }

    public long getRefreshTokenMaxAge() {
        return REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60;
    }

    private Instant genAccessTokenExpiration() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .plusMinutes(ACCESS_TOKEN_EXPIRATION_MINUTES)
                .toInstant();
    }

    private Instant genRefreshTokenExpiration() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .plusDays(REFRESH_TOKEN_EXPIRATION_DAYS)
                .toInstant();
    }

}
