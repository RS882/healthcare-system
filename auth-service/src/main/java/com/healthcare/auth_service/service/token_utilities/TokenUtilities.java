package com.healthcare.auth_service.service.token_utilities;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class TokenUtilities {

    public static String extractJwtFromRequest(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        log.warn("Authorization header is missing or malformed: {}", authHeader);
        return null;
    }
}
