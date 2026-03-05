package com.healthcare.user_service.filter.security;

import io.jsonwebtoken.Claims;

import java.util.List;


public record SignedUserContext(
        String userId,
        List<String> roles,
        String requestId,
        String version
) {
    public static SignedUserContext from(Claims c) {
        String userId = c.getSubject();
        String rid = c.get("rid", String.class);
        String ver = c.get("ver", String.class);

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) c.get("roles", List.class);

        return new SignedUserContext(userId, roles == null ? List.of() : List.copyOf(roles), rid, ver);
    }
}
