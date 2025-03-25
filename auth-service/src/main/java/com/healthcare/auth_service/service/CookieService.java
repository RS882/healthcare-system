package com.healthcare.auth_service.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    public static final String REFRESH_TOKEN = "Refresh-token";

    public final String PATH = "/api/v1/auth/refresh";

    public void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = makeCookie(REFRESH_TOKEN, refreshToken, refreshExpiration, PATH);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void removeRefreshTokenFromCookie(HttpServletResponse response) {
        ResponseCookie cookie = makeCookie(REFRESH_TOKEN, "", 0, PATH);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(REFRESH_TOKEN)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie makeCookie(String name, String value, long maxAge, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path(path)
                .maxAge(maxAge)
                .sameSite("None")
                .build();
    }
}
