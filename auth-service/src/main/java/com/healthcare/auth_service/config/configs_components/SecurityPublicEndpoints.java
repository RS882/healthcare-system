package com.healthcare.auth_service.config.configs_components;

public final class SecurityPublicEndpoints {

    public static final String[] PUBLIC_ENDPOINTS = {

            "/swagger-ui.html",
            "/swagger-ui/**",

            "/v3/api-docs",
            "/v3/api-docs/**",

            "/error"
    };

    private SecurityPublicEndpoints() {
    }

}
