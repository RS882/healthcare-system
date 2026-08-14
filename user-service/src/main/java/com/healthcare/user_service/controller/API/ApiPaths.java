package com.healthcare.user_service.controller.API;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String USERS_BASIC_URL = "/v1/users";

    public static final String REGISTRATION = "/registration";

    public static final String REGISTRATION_URL = USERS_BASIC_URL + REGISTRATION;

    public static final String INTERNAL = "/internal";

    public static final String INTERNAL_BASIC_URL = USERS_BASIC_URL + INTERNAL;

    public static final String LOOKUP = "/lookup";

    public static final String INTERNAL_LOOKUP = INTERNAL + LOOKUP;

    public static final String INTERNAL_LOOKUP_URL = INTERNAL_BASIC_URL + LOOKUP;

    public static final String INTERNAL_ALL_URL = INTERNAL_BASIC_URL + "/**";

    public static final String PATH_VARIABLE_ID = "id";

    public static final String BY_ID = "/id/{" + PATH_VARIABLE_ID + "}";

    public static final String BY_ID_URL = USERS_BASIC_URL + BY_ID;
}
