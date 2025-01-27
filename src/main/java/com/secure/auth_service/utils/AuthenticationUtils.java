package com.secure.auth_service.utils;

import com.secure.auth_service.enums.Authority;
import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.models.User;
import com.secure.auth_service.services.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthenticationUtils {

    private final TokenUtils tokenUtils;
    private final CookieUtils cookieUtils;
    private final RedisService redisService;

    public boolean tryAutoLogin(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieUtils.getTokenFromCookie(request, "access_token");
        String refreshToken = cookieUtils.getTokenFromCookie(request, "refresh_token");

        if (tokenUtils.validateAccessToken(accessToken)) {
            handleValidAccessToken(accessToken);
            return true;
        } else if (refreshToken != null) {
            return handleRefreshToken(request, response, refreshToken);
        }
        return false;
    }

    private void handleValidAccessToken(String accessToken) {
        String login = tokenUtils.getLoginFromToken(accessToken);
        String role = tokenUtils.getRoleFromToken(accessToken);
        Set<Authority> authorities = tokenUtils.getAuthoritiesFromToken(accessToken);

        User user = new User();
        user.setId(UUID.fromString(tokenUtils.getIdFromToken(accessToken)));
        user.setLogin(login);
        user.setRole(Roles.valueOf(role));
        user.setAuthorities(authorities);

        setAuthentication(user);
    }

    private boolean handleRefreshToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        String idToken = cookieUtils.getTokenFromCookie(request, "id_token");
        if (idToken == null) return false;

        User user = tokenUtils.getUserFromIdToken(idToken);
        if (user == null) return false;

        if (!tokenUtils.validateRefreshToken(refreshToken, user.getId().toString())) {
            return false;
        }

        refreshAllTokens(user, response);
        setAuthentication(user);
        return true;
    }

    public void refreshAllTokens(User user, HttpServletResponse response) {
        redisService.invalidateRefreshToken(user.getId().toString());

        String newAccessToken = tokenUtils.generateAccessToken(user);
        String newRefreshToken = tokenUtils.generateRefreshToken(user);
        String newIdToken = tokenUtils.generateIdToken(user);

        redisService.addToRefreshWhitelist(
                user.getId().toString(),
                newRefreshToken,
                tokenUtils.getRefreshTokenMaxAge()
        );

        List<ResponseCookie> cookies = List.of(
                cookieUtils.createCookie("access_token", true, newAccessToken, tokenUtils.getAccessTokenMaxAge()),
                cookieUtils.createCookie("refresh_token", true, newRefreshToken, tokenUtils.getRefreshTokenMaxAge()),
                cookieUtils.createCookie("id_token", false, newIdToken, tokenUtils.getRefreshTokenMaxAge())
        );

        cookieUtils.addCookiesToResponse(response, cookies);
    }

    public void setAuthentication(User user) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}