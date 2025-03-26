package com.healthcare.auth_service.service.token_utilities;

import jakarta.servlet.http.HttpServletRequest;

public class TokenUtilities {

    public static String extractJwtFromRequest(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }
}
