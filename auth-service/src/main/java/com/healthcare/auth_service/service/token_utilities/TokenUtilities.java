package com.healthcare.auth_service.service.token_utilities;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

@Slf4j
public class TokenUtilities {

    public static String extractJwtFromRequest(HttpServletRequest request) {
        return extractJwtFromHeader(request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    public static String extractJwtFromRequest(NativeWebRequest webRequest) {
        return extractJwtFromHeader(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    private static String extractJwtFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        return null;
    }
}
