package com.healthcare.user_service.security.internal_request.constant;

public enum InternalAuthority {

    USER_LOOKUP,
    USER_AUTH_INFO;

    public String authority() {
        return name();
    }
}