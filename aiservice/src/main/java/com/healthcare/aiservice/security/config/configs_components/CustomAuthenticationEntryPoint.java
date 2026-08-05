package com.healthcare.aiservice.security.config.configs_components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.exception.UserAuthInfoNotFoundException;
import com.healthcare.aiservice.exception.UserServiceUnavailableException;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {

        if (response.isCommitted()) {
            log.warn("Response already committed. Skipping error writing.");
            return;
        }
        HttpStatus status = resolveStatus(exception);

        int statusValue = status.value();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusValue);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(statusValue)
                .error(status.getReasonPhrase())
                .message(resolveClientMessage(exception))
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getOutputStream(), error);

        log.warn(
                "Authentication failed. path={}, message={}",
                request.getRequestURI(),
                exception.getMessage(),
                exception
        );
    }

    private HttpStatus resolveStatus(AuthenticationException ex) {

        if (ex instanceof UserServiceUnavailableException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (ex instanceof UserAuthInfoNotFoundException) {
            return HttpStatus.UNAUTHORIZED;
        }

        return HttpStatus.UNAUTHORIZED;
    }

    private String resolveClientMessage(
            AuthenticationException exception
    ) {
        if (exception instanceof UserServiceUnavailableException) {
            return "Authentication service is temporarily unavailable";
        }

        if (exception instanceof UserAuthInfoNotFoundException) {
            return "Authentication failed";
        }

        return "Authentication failed";
    }
}

