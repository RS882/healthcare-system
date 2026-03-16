package com.healthcare.user_service.controller.API;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String USER_BASIC_URL = "/v1/users";

    public static final String REGISTRATION = "/registration";

    public static final String REGISTRATION_URL = USER_BASIC_URL + REGISTRATION;

    public static final String LOOKUP = "/lookup";

    public static final String LOOKUP_URL = USER_BASIC_URL + LOOKUP;

    public static final String PATH_VARIABLE_ID = "id";

    public static final String BY_ID = "/id/{" + PATH_VARIABLE_ID + "}";

    public static final String BY_ID_URL = USER_BASIC_URL + BY_ID;

}
