package com.secure.auth_service.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CookieUtils {

    public String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearAuthCookies(HttpServletResponse response) {
        List<ResponseCookie> cookies = new ArrayList<>();
        cookies.add(createCookie("access_token", true, "", 0));
        cookies.add(createCookie("refresh_token", true, "", 0));
        cookies.add(createCookie("id_token", false, "", 0));
        addCookiesToResponse(response, cookies);
    }

    public ResponseCookie createCookie(String name, boolean httpOnly, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }

    public void addCookiesToResponse(HttpServletResponse response, List<ResponseCookie> cookies) {
        for (ResponseCookie cookie : cookies) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }
}