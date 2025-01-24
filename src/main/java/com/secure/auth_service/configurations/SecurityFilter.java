package com.secure.auth_service.configurations;

import com.secure.auth_service.utils.AuthenticationUtils;
import com.secure.auth_service.utils.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final AuthenticationUtils authUtils;
    private final CookieUtils cookieUtils;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        if (isExcluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authUtils.tryAutoLogin(request, response)) {
            cookieUtils.clearAuthCookies(response);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        return ("/auth/login".equals(uri) && "POST".equalsIgnoreCase(method)) ||
                ("/auth/register".equals(uri) && "POST".equalsIgnoreCase(method)) ||
                ("/auth/refresh".equals(uri) && "POST".equalsIgnoreCase(method));
    }
}