package com.healthcare.auth_service.controller.API;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String AUTH_BASIC_URL = "/v1/auth";

    public static final String LOGIN = "/login";

    public static final String LOGIN_URL = AUTH_BASIC_URL + LOGIN;

    public static final String REFRESH = "/refresh";

    public static final String REFRESH_URL = AUTH_BASIC_URL + REFRESH;

    public static final String LOGOUT = "/logout";

    public static final String LOGOUT_URL = AUTH_BASIC_URL + LOGOUT;
}
