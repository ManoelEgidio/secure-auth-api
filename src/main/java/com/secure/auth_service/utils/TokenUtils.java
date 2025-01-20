package com.secure.auth_service.utils;

import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TokenUtils {

    @Value("${jwt.private-key}")
    private String privateKeyPem;

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    @Value("${jwt.access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.id.token.expiration}")
    private long idTokenExpiration;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(privateKeyPem);
            this.publicKey = loadPublicKey(publicKeyPem);
            this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        } catch (Exception e) {
            throw new CustomException("Falha ao carregar as chaves RSA para JWT: " + e.getMessage());
        }
    }

    private RSAPrivateKey loadPrivateKey(String key) {
        try {
            String privateKeyContent = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new CustomException("Erro ao carregar a chave privada: " + e.getMessage());
        }
    }

    private RSAPublicKey loadPublicKey(String key) {
        try {
            String publicKeyContent = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new CustomException("Erro ao carregar a chave pública: " + e.getMessage());
        }
    }

    public String generateAccessToken(User user) {
        try {
            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return JWT.create()
                    .withIssuer("auth-service")
                    .withSubject(user.getLogin())
                    .withClaim("role", user.getRole().name())
                    .withClaim("authority", authorities)
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
                    .sign(algorithm);
        } catch (Exception e) {
            throw new CustomException("Falha ao gerar o Access Token: " + e.getMessage());
        }
    }

    public String generateRefreshToken(User user) {
        try {
            return JWT.create()
                    .withIssuer("auth-service")
                    .withSubject(user.getLogin())
                    .withClaim("type", "refresh")
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(Instant.now().plusMillis(refreshTokenExpiration)))
                    .sign(algorithm);
        } catch (Exception e) {
            throw new CustomException("Falha ao gerar o Refresh Token: " + e.getMessage());
        }
    }

    public String generateIdToken(User user) {
        try {
            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return JWT.create()
                    .withIssuer("auth-service")
                    .withSubject(user.getLogin())
                    .withClaim("id", user.getId().toString())
                    .withClaim("name", user.getName())
                    .withClaim("role", user.getRole().name())
                    .withClaim("authority", authorities)
                    .withExpiresAt(Date.from(Instant.now().plusMillis(idTokenExpiration)))
                    .sign(algorithm);
        } catch (Exception e) {
            throw new CustomException("Falha ao gerar o ID Token: " + e.getMessage());
        }
    }

    public User getUserFromIdToken(String idToken) {
        try {
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("auth-service")
                    .build()
                    .verify(idToken);

            User user = new User();

            String idStr = jwt.getClaim("id").asString();
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    user.setId(UUID.fromString(idStr));
                } catch (IllegalArgumentException e) {
                    throw new CustomException("Formato inválido para o 'id' no token: " + e.getMessage());
                }
            } else {
                throw new CustomException("O claim 'id' está ausente ou é vazio no token.");
            }

            String login = jwt.getSubject();
            if (login != null && !login.isEmpty()) {
                user.setLogin(login);
            } else {
                throw new CustomException("O token não contém um 'login' válido.");
            }

            String roleStr = jwt.getClaim("role").asString();
            if (roleStr != null && !roleStr.isEmpty()) {
                try {
                    user.setRole(Roles.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    throw new CustomException("Role inválido no token: " + e.getMessage());
                }
            } else {
                throw new CustomException("O token não contém um 'role' válido.");
            }

            String name = jwt.getClaim("name").asString();
            if (name != null && !name.isEmpty()) {
                user.setName(name);
            } else {
                throw new CustomException("O token não contém um 'name' válido.");
            }

            return user;
        } catch (JWTVerificationException e) {
            throw new CustomException("Erro ao verificar o ID Token: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException("Erro ao reconstruir usuário do ID token: " + e.getMessage());
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, false);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, true);
    }

    private boolean validateToken(String token, boolean isRefreshToken) {
        try {
            var verifier = JWT.require(algorithm)
                    .withIssuer("auth-service");
            if (isRefreshToken) {
                verifier.withClaim("type", "refresh");
            }
            verifier.build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getLoginFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            throw new CustomException("Erro ao extrair o login do token: " + e.getMessage());
        }
    }

    public String getRoleFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            String role = jwt.getClaim("role").asString();
            if (role == null || role.isEmpty()) {
                throw new CustomException("O token não contém um 'role' válido.");
            }
            return role;
        } catch (Exception e) {
            throw new CustomException("Erro ao extrair o role do token: " + e.getMessage());
        }
    }

    public long getAccessTokenMaxAge() {
        return accessTokenExpiration / 1000;
    }

    public long getRefreshTokenMaxAge() {
        return refreshTokenExpiration / 1000;
    }

    public long getIdTokenMaxAge() {
        return idTokenExpiration / 1000;
    }
}
