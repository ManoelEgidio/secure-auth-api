package com.secure.auth_service.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    private static final String TEST_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC5o4bdRL2i74Zu
            4Wxkmw6CrfmoVWzHvfS2WqnQR07tnRVR+HLr7bhOjN2ENwF09HBefZMrQcOVNgM9
            Oqp4L+jQL/o/uhce0wqvp3kcr+zxI4ThvhDkWOJJvI3+Sitw4mGJ7p2DinZ1ofMD
            Y+RSfv21IjQ9S0pVXyeir3ONZlg2J4JwljlMz5Q0XBJyE3uC5tp44LMa2uWk/uy6
            YWZAi/YjGEKHYlRa3TZgt9b7spkLeTADeQpyjPZOhpxilnDh6mRdV3hQ6gl7E2z6
            x0SIHLC5Wqh82mlACxM6PxY5UJTsnvYy1Ko6IdXlwUxY+M3Ost/n65y6ruM/mJta
            XxXDR9P5AgMBAAECggEACK7bhEvZPemLuscnAYas1VPLZsJbefVKi2FZajanpwlv
            AZxIIyfFnuuhAd9BHXDAinQ/txPWpjWg+FMfPaC/YvDXjksHQVJtlcfqLDtrWbUd
            Bk0UWltGTFzPhx/TgS50PFYm/naHx2/0NLSjrZsnL/rXVtrycWSnxIjbNAzf3o/X
            ugDzpZP8WuXN28+jqa4QrFNA9EE4qoP3XbX++TSPgRssID2ysFWNibYJJXoH9/sY
            9NelGTy/VVin1hMAI/oJ2YZwpHOoHKie4NxJiDyaI6I5YTTq92bSWPrq8eqvRAom
            CZhYd2dtWWeK2hYsWo6rSpylspeuXLRMQZQvk7XjzQKBgQD+4OdPmkqR3Kx3xIlL
            hPr4w9/WVt43P/f5CkRL856ym47jmFrTPZxo74ZP4ExCXiT4d95B9EiCMwi2VETy
            VJaQQD1b7xH4Ge8ijZWL9CZnvlBabgQ9sNfREwhnZsDkJAvoPfOLU/qJDp/5DBQp
            S7atZNS5Wcx8/O1wipGkIMScFQKBgQC6dKGZ2aJOgi4v/P61zr7qZP9rWSOnPdP0
            56r3wFhNVc3fkFWIFblLKWwjMMObAo/JkI0c64rMv3SHsE+gDmpiac/Ks8lPCK7M
            vDJrQCOoExGDgTBVxqf7GANZl1VSM3TZ9rZmJiPJEKa/6xA2k4hX9TF2BRuGkYE5
            +sDpNlM9VQKBgFP5FTyMwPS9l7T9854F+gnrvtuHUz8wvCo4z7eWVDrUNYeWspXW
            Pqn0AHRgmb9j9RpHo9pm+VysI7qumOieJdzwzUZ0xZ7QLJFdxF+P2PdlJGQmyw7o
            LLKdaq9mkzaXCbVCc/L7gBeX0ezjpeDVKSKHje5jP5QOYMwrW0CP6zbhAoGBALc+
            WHNzR+gQWIQhB9J11XtWtw8xE7EROnW+GHBUQHQAl8vXdbfAYdJUa0pKanK1Gcrm
            nUiG1GpU3plb1WVfxX3ir6xu9LEPv3rGmmZ9Ko3L77JmDLLAAebrup8Z82hN2dE8
            fhX19cVs7/Q9eDmq1JxdBLcQXfTqmy34Gg8YrMXhAoGASVWiDm7rkNjC/qE5DVLQ
            GDVx0I9p3dazQ/+fSeJUFYFMWwrvCamMkBytl6NWKNkOirBvvXKUAWZvI8AdZN0f
            dT5L48w5OmeUbhxjyiPEiTjlYuoni9ARbZTvVw9mVshA5tPi/tQYPm17ZJyJJWcw
            DDYtiRRz/p5Q4lFF2/ZHhfE=
            -----END PRIVATE KEY-----
            """;

    private static final String TEST_PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuaOG3US9ou+GbuFsZJsO
            gq35qFVsx730tlqp0EdO7Z0VUfhy6+24TozdhDcBdPRwXn2TK0HDlTYDPTqqeC/o
            0C/6P7oXHtMKr6d5HK/s8SOE4b4Q5FjiSbyN/korcOJhie6dg4p2daHzA2PkUn79
            tSI0PUtKVV8noq9zjWZYNieCcJY5TM+UNFwSchN7gubaeOCzGtrlpP7sumFmQIv2
            IxhCh2JUWt02YLfW+7KZC3kwA3kKcoz2ToacYpZw4epkXVd4UOoJexNs+sdEiByw
            uVqofNppQAsTOj8WOVCU7J72MtSqOiHV5cFMWPjNzrLf5+ucuq7jP5ibWl8Vw0fT
            +QIDAQAB
            -----END PUBLIC KEY-----
            """;

    private Algorithm algorithm;

    @BeforeEach
    void setUp() throws Exception {
        tokenUtils.privateKeyPem = TEST_PRIVATE_KEY;
        tokenUtils.publicKeyPem = TEST_PUBLIC_KEY;
//        tokenUtils.accessTokenExpiration = 3600000;
//        tokenUtils.refreshTokenExpiration = 7200000;
//        tokenUtils.idTokenExpiration = 3600000;
        tokenUtils.init();

        Field algorithmField = TokenUtils.class.getDeclaredField("algorithm");
        algorithmField.setAccessible(true);
        algorithm = (Algorithm) algorithmField.get(tokenUtils);
    }

    @Test
    void generateAccessToken_Success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.CREATE))
                .enabled(true)
                .build();

        String token = tokenUtils.generateAccessToken(user);
        assertNotNull(token);

        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("auth-service", decodedJWT.getIssuer());
        assertEquals(user.getLogin(), decodedJWT.getSubject());
        assertEquals(user.getRole().name(), decodedJWT.getClaim("role").asString());
        assertTrue(decodedJWT.getClaim("authority").asList(String.class).contains("CREATE"));
        assertNotNull(decodedJWT.getIssuedAt());
        assertNotNull(decodedJWT.getExpiresAt());
    }

    @Test
    void generateAccessToken_JWTCreationException() {
        User user = null;
        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.generateAccessToken(user));
        assertTrue(exception.getMessage().contains("Falha ao gerar o Access Token"));
    }

    @Test
    void generateRefreshToken_Success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.SEARCH))
                .enabled(true)
                .build();

        String token = tokenUtils.generateRefreshToken(user);
        assertNotNull(token);

        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("auth-service", decodedJWT.getIssuer());
        assertEquals(user.getLogin(), decodedJWT.getSubject());
        assertEquals("refresh", decodedJWT.getClaim("type").asString());
        assertNotNull(decodedJWT.getIssuedAt());
        assertNotNull(decodedJWT.getExpiresAt());
    }

    @Test
    void generateRefreshToken_JWTCreationException() {
        User user = null;
        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.generateRefreshToken(user));
        assertTrue(exception.getMessage().contains("Falha ao gerar o Refresh Token"));
    }

    @Test
    void generateIdToken_Success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.ADMIN)
                .authorities(Set.of(Authority.EDIT))
                .enabled(true)
                .build();

        String token = tokenUtils.generateIdToken(user);
        assertNotNull(token);

        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("auth-service", decodedJWT.getIssuer());
        assertEquals(user.getLogin(), decodedJWT.getSubject());
        assertEquals(user.getId().toString(), decodedJWT.getClaim("id").asString());
        assertEquals(user.getName(), decodedJWT.getClaim("name").asString());
        assertEquals(user.getRole().name(), decodedJWT.getClaim("role").asString());
        assertTrue(decodedJWT.getClaim("authority").asList(String.class).contains("EDIT"));
        assertNotNull(decodedJWT.getExpiresAt());
    }

    @Test
    void generateIdToken_JWTCreationException() {
        User user = null;
        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.generateIdToken(user));
        assertTrue(exception.getMessage().contains("Falha ao gerar o ID Token"));
    }

    @Test
    void getUserFromIdToken_Success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();

        String token = tokenUtils.generateIdToken(user);
        User extractedUser = tokenUtils.getUserFromIdToken(token);

        assertNotNull(extractedUser);
        assertEquals(user.getId(), extractedUser.getId());
        assertEquals(user.getLogin(), extractedUser.getLogin());
        assertEquals(user.getRole(), extractedUser.getRole());
        assertEquals(user.getName(), extractedUser.getName());
    }

    @Test
    void getUserFromIdToken_InvalidId() {
        String token = JWT.create()
                .withIssuer("auth-service")
                .withSubject("test.user@example.com")
                .withClaim("id", "invalid-uuid")
                .withClaim("name", "Test User")
                .withClaim("role", "USER")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getUserFromIdToken(token));
        assertTrue(exception.getMessage().contains("Formato inválido para o 'id' no token"));
    }

    @Test
    void getUserFromIdToken_MissingId() {
        String token = JWT.create()
                .withIssuer("auth-service")
                .withSubject("test.user@example.com")
                .withClaim("name", "Test User")
                .withClaim("role", "USER")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getUserFromIdToken(token));
        assertTrue(exception.getMessage().contains("O claim 'id' está ausente ou é vazio no token"));
    }

    @Test
    void getUserFromIdToken_InvalidRole() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();

        String token = JWT.create()
                .withIssuer("auth-service")
                .withSubject(user.getLogin())
                .withClaim("id", user.getId().toString())
                .withClaim("name", user.getName())
                .withClaim("role", "INVALID_ROLE")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getUserFromIdToken(token));
        assertTrue(exception.getMessage().contains("Role inválido no token"));
    }

    @Test
    void getUserFromIdToken_MissingLogin() {
        String token = JWT.create()
                .withIssuer("auth-service")
                .withClaim("id", UUID.randomUUID().toString())
                .withClaim("name", "Test User")
                .withClaim("role", "USER")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getUserFromIdToken(token));
        assertTrue(exception.getMessage().contains("O token não contém um 'login' válido"));
    }

    @Test
    void getUserFromIdToken_MissingName() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();

        String token = JWT.create()
                .withIssuer("auth-service")
                .withSubject(user.getLogin())
                .withClaim("id", user.getId().toString())
                .withClaim("role", "USER")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getUserFromIdToken(token));
        assertTrue(exception.getMessage().contains("O token não contém um 'name' válido"));
    }

    @Test
    void getUserFromIdToken_JWTVerificationException() {
        String token = "invalid.token.string";

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getUserFromIdToken(token));
        assertTrue(exception.getMessage().contains("Erro ao verificar o ID Token"));
    }

    @Test
    void validateAccessToken_Valid() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.CREATE))
                .enabled(true)
                .build();

        String token = tokenUtils.generateAccessToken(user);
        assertTrue(tokenUtils.validateAccessToken(token));
    }

    @Test
    void validateAccessToken_Invalid() {
        String token = "invalid.token.string";
        assertFalse(tokenUtils.validateAccessToken(token));
    }

    @Test
    void validateRefreshToken_Valid() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.SEARCH))
                .enabled(true)
                .build();

        String token = tokenUtils.generateRefreshToken(user);
        assertTrue(tokenUtils.validateRefreshToken(token));
    }

    @Test
    void validateRefreshToken_Invalid() {
        String token = "invalid.token.string";
        assertFalse(tokenUtils.validateRefreshToken(token));
    }

    @Test
    void validateRefreshToken_WrongType() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.SEARCH))
                .enabled(true)
                .build();

        String token = tokenUtils.generateAccessToken(user);
        assertFalse(tokenUtils.validateRefreshToken(token));
    }

    @Test
    void getLoginFromToken_Success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.USER)
                .authorities(Set.of(Authority.VIEW))
                .enabled(true)
                .build();

        String token = tokenUtils.generateAccessToken(user);
        String login = tokenUtils.getLoginFromToken(token);
        assertEquals(user.getLogin(), login);
    }

    @Test
    void getLoginFromToken_InvalidToken() {
        String token = "invalid.token.string";
        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getLoginFromToken(token));
        assertTrue(exception.getMessage().contains("Erro ao extrair o login do token"));
    }

    @Test
    void getRoleFromToken_Success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .login("test.user@example.com")
                .role(Roles.ADMIN)
                .authorities(Set.of(Authority.EDIT))
                .enabled(true)
                .build();

        String token = tokenUtils.generateIdToken(user);
        String role = tokenUtils.getRoleFromToken(token);
        assertEquals(user.getRole().name(), role);
    }

    @Test
    void getRoleFromToken_MissingRole() {
        String token = JWT.create()
                .withIssuer("auth-service")
                .withSubject("test.user@example.com")
                .withClaim("id", UUID.randomUUID().toString())
                .withClaim("name", "Test User")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getRoleFromToken(token));
        assertTrue(exception.getMessage().contains("O token não contém um 'role' válido"));
    }

    @Test
    void getRoleFromToken_InvalidRole() {
        String token = JWT.create()
                .withIssuer("auth-service")
                .withSubject("test.user@example.com")
                .withClaim("id", UUID.randomUUID().toString())
                .withClaim("name", "Test User")
                .withClaim("role", "INVALID_ROLE")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        String role = tokenUtils.getRoleFromToken(token);
        assertEquals("INVALID_ROLE", role);
    }

    @Test
    void getRoleFromToken_InvalidToken() {
        String token = "invalid.token.string";
        CustomException exception = assertThrows(CustomException.class, () -> tokenUtils.getRoleFromToken(token));
        assertTrue(exception.getMessage().contains("Erro ao extrair o role do token"));
    }

    @Test
    void getAccessTokenMaxAge() {
        assertEquals(3600, tokenUtils.getAccessTokenMaxAge());
    }

    @Test
    void getRefreshTokenMaxAge() {
        assertEquals(7200, tokenUtils.getRefreshTokenMaxAge());
    }

    @Test
    void getIdTokenMaxAge() {
        assertEquals(3600, tokenUtils.getRefreshTokenMaxAge());
    }
}
