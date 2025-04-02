package com.healthcare.auth_service.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

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

    private ResponseCookie makeCookie(String name, String value, long maxAge, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                //TODO for https - make secure true
                .secure(false)
                .path(path)
                .maxAge(maxAge)
                //TODO for https
//                .sameSite("None")
                .build();
    }
}
