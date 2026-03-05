package com.healthcare.user_service.filter.security.interfaces;

import io.jsonwebtoken.Claims;

public interface UserContextVerifier {

    Claims verifyAndGetClaims(String token);
}
