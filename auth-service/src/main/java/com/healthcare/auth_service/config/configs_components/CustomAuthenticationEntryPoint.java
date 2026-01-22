package com.healthcare.auth_service.config.configs_components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import com.healthcare.auth_service.exception_handler.exception.JwtAuthenticationException;
import com.healthcare.auth_service.exception_handler.exception.RequestIdAuthenticationException;
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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        if (response.isCommitted()) {
            log.debug("Response already committed. Skipping error writing.");
            return;
        }

        Throwable t = authException;
        while (t != null) {

            if (t instanceof RequestIdAuthenticationException ex) {
                log.debug("RequestId validation failed: {}", ex.getMessage());
                writeError(request, response, ex.getStatus(), List.of(ex.getMessage()));
                return;
             }

            if (t instanceof JwtAuthenticationException ex) {
                log.debug("JWT authentication failed: {}", ex.getMessage());
                writeError(request, response, HttpStatus.UNAUTHORIZED,
                        List.of("Unauthorized access", ex.getMessage()));
                return;
            }

            t = t.getCause();
        }

        log.debug("Authentication failed: {}", authException.getMessage(), authException);
        writeError(request, response, HttpStatus.UNAUTHORIZED, List.of("Unauthorized access"));
    }

    private void writeError(HttpServletRequest request,
                            HttpServletResponse response,
                            HttpStatus status,
                            List<String> messages) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status.value());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(messages)
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}

