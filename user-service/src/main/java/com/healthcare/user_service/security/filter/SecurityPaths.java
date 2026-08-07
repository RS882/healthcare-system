package com.healthcare.user_service.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public enum SecurityPaths {

    ACTUATOR("/actuator"),
    SWAGGER_UI("/swagger-ui"),
    API_DOCS("/v3/api-docs");

    private final String path;

    SecurityPaths(String path) {
        this.path = path;
    }

    public static boolean shouldSkipSecurity(
            HttpServletRequest request
    ) {
        if (request == null) {
            return false;
        }

        String servletPath = request.getServletPath();

        if (!StringUtils.hasText(servletPath)) {
            return false;
        }

        return Arrays.stream(values())
                .anyMatch(securityPath ->
                        securityPath.matches(servletPath)
                );
    }

    private boolean matches(String requestPath) {
        return requestPath.equals(path)
                || requestPath.startsWith(path + "/")
                || requestPath.startsWith(path + ".");
    }
}