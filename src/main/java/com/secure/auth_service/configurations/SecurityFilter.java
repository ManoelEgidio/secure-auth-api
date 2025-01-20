package com.secure.auth_service.configurations;

import com.secure.auth_service.enums.Roles;
import com.secure.auth_service.exceptions.CustomException;
import com.secure.auth_service.models.User;
import com.secure.auth_service.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (isExcluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = recoverTokenFromCookie(request, "access_token");
        String refreshToken = recoverTokenFromCookie(request, "refresh_token");

        boolean isAccessTokenValid = accessToken != null && tokenUtils.validateAccessToken(accessToken);
        boolean isRefreshTokenValid = refreshToken != null && tokenUtils.validateRefreshToken(refreshToken);

        if (isAccessTokenValid) {
            String login = tokenUtils.getLoginFromToken(accessToken);
            authenticateUser(login, accessToken);
        } else if (isRefreshTokenValid) {
            String login = tokenUtils.getLoginFromToken(refreshToken);
            authenticateUserWithTokenRefresh(login, request, response);
        } else {
            removeAuthCookies(response);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        return ("/auth/login".equals(uri) && "POST".equalsIgnoreCase(method)) ||
                ("/auth/register".equals(uri) && "POST".equalsIgnoreCase(method));
    }

    private void authenticateUser(String login, String accessToken) {
        if (login == null) return;

        String roleStr = tokenUtils.getRoleFromToken(accessToken);

        User user = new User();
        user.setLogin(login);
        try {
            user.setRole(Roles.valueOf(roleStr.toUpperCase())); // Usar o nome correto da enum
        } catch (IllegalArgumentException e) {
            throw new CustomException("Role inválido no token: " + e.getMessage());
        }

        setAuthentication(user);
    }

    private void authenticateUserWithTokenRefresh(String login, HttpServletRequest request, HttpServletResponse response) {
        if (login == null) {
            removeAuthCookies(response);
            return;
        }

        String idToken = recoverTokenFromCookie(request, "id_token");
        if (idToken == null) {
            removeAuthCookies(response);
            return;
        }

        User user = tokenUtils.getUserFromIdToken(idToken);

        String newAccessToken = tokenUtils.generateAccessToken(user);
        String newRefreshToken = tokenUtils.generateRefreshToken(user);
        String newIdToken = tokenUtils.generateIdToken(user);

        long accessTokenMaxAge = tokenUtils.getAccessTokenMaxAge();
        long refreshTokenMaxAge = tokenUtils.getRefreshTokenMaxAge();
        long idTokenMaxAge = tokenUtils.getIdTokenMaxAge();

        List<ResponseCookie> cookies = new ArrayList<>();
        cookies.add(createCookie("access_token", true, newAccessToken, accessTokenMaxAge));
        cookies.add(createCookie("refresh_token", true, newRefreshToken, refreshTokenMaxAge));
        cookies.add(createCookie("id_token", false, newIdToken, idTokenMaxAge));

        addCookiesToResponse(response, cookies);

        setAuthentication(user);
    }

    private String recoverTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void removeAuthCookies(HttpServletResponse response) {
        List<ResponseCookie> cookies = new ArrayList<>();
        cookies.add(createCookie("access_token", true, "", 0));
        cookies.add(createCookie("refresh_token", true, "", 0));
        cookies.add(createCookie("id_token", false, "", 0));

        addCookiesToResponse(response, cookies);
    }

    private ResponseCookie createCookie(String name, boolean httpOnly, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }

    private void addCookiesToResponse(HttpServletResponse response, List<ResponseCookie> cookies) {
        for (ResponseCookie cookie : cookies) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }

    private void setAuthentication(User userDetails) {
        if (userDetails != null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
