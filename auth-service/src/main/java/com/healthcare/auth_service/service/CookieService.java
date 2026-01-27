package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.healthcare.auth_service.service.constant.RefreshTokenTitle.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final JwtProperties jwtProps;

    public void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = makeCookie(REFRESH_TOKEN, refreshToken, jwtProps.refreshTokenExpiration(), jwtProps.cookiePath());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void removeRefreshTokenFromCookie(HttpServletResponse response) {
        ResponseCookie cookie = makeCookie(REFRESH_TOKEN, "", Duration.ZERO, jwtProps.cookiePath());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private ResponseCookie makeCookie(String name, String value, Duration maxAge, String path) {
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
