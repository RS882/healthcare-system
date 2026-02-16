package com.healthcare.api_gateway.filter.constant;

import com.healthcare.api_gateway.utilite.AttrKey;

import java.util.List;

import static com.healthcare.api_gateway.filter.constant.ContextAttrNames.*;

public final class AttrKeys {
    private AttrKeys() {}

    public static final AttrKey<String> REQUEST_ID_ATTR_KEY = AttrKey.of(ATTR_REQUEST_ID);
    public static final AttrKey<Long> USER_ID_ATTR_KEY = AttrKey.of( ATTR_USER_ID);
    public static final AttrKey<List<String>> USER_ROLES_ATTR_KEY = AttrKey.of(ATTR_USER_ROLES);
}
