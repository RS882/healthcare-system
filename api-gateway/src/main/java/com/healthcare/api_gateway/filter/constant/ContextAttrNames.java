package com.healthcare.api_gateway.filter.constant;

public class ContextAttrNames {
    private ContextAttrNames() {
    }

    private static final String ATTR_PREFIX = "attr.";

    public static final String ATTR_REQUEST_ID = ATTR_PREFIX + "requestId";
    public static final String ATTR_USER_ID = ATTR_PREFIX + "userId";
    public static final String ATTR_USER_ROLES = ATTR_PREFIX + "userRoles";
}
