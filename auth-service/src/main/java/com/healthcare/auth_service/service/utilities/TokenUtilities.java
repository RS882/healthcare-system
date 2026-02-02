package com.healthcare.auth_service.service.utilities;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.*;
import java.util.function.Function;


@Slf4j
public class TokenUtilities {

    public static String extractJwtFromRequest(HttpServletRequest request) {
        return extractJwtFromHeaders(
                Collections.list(request.getHeaderNames()),
                request::getHeader
        );
    }

    public static String extractJwtFromRequest(NativeWebRequest webRequest) {
        List<String> headerNames = new ArrayList<>();

        Iterator<String> it = webRequest.getHeaderNames();
        while (it.hasNext()) {
            headerNames.add(it.next());
        }

        return extractJwtFromHeaders(
                headerNames,
                webRequest::getHeader
        );
    }

    private static String extractJwtFromHeaders(
            Collection<String> headerNames,
            Function<String, String> headerResolver
    ) {
        if (headerNames == null) {
            return null;
        }

        for (String headerName : headerNames) {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName)) {
                return extractJwtFromHeader(headerResolver.apply(headerName));
            }
        }
        return null;
    }

    private static String extractJwtFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        return null;
    }
}

