package com.healthcare.user_service.controller.API;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String USER_BASIC_URL = "/v1/auth";

    public static final String REGISTRATION = "/registration";

    public static final String REGISTRATION_URL = USER_BASIC_URL + REGISTRATION;

    public static final String BY_EMAIL = "/email/{email}";

    public static final String BY_EMAIL_URL = USER_BASIC_URL + BY_EMAIL;

    public static final String BY_ID = "/id/{id}";

    public static final String BY_ID_URL = USER_BASIC_URL + BY_ID;

}
