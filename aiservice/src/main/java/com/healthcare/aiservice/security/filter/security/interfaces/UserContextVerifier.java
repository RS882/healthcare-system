package com.healthcare.aiservice.security.filter.security.interfaces;

import io.jsonwebtoken.Claims;

public interface UserContextVerifier {

    Claims verifyAndGetClaims(String token);
}
