package com.healthcare.api_gateway.filter;

import java.util.List;

public record AuthContext(Long userId, List<String> roles) {}
