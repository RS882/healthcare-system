package com.healthcare.auth_service.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CookieService {

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${jwt.cookie-path}")
    private String path;

    public static final String REFRESH_TOKEN = "Refresh-token";

    public void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = makeCookie(REFRESH_TOKEN, refreshToken, refreshExpirationMs, path);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void removeRefreshTokenFromCookie(HttpServletResponse response) {
        ResponseCookie cookie = makeCookie(REFRESH_TOKEN, "", 0, path);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(REFRESH_TOKEN)) {
                String value = cookie.getValue();
                if (StringUtils.hasText(value)) {
                    return value;
                }
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
