package com.healthcare.aiservice.security.config.configs_components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        if (response.isCommitted()) {
            log.debug("Response already committed. Skipping error writing.");
            return;
        }

        final HttpStatus STATUS = HttpStatus.FORBIDDEN;
        final int STATUS_VALUE = STATUS.value();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(STATUS_VALUE)
                .error(STATUS.getReasonPhrase())
                .message(String.format(
                        "Access denied for URL: %s, message: %s",
                        request.getRequestURI(),
                        accessDeniedException.getMessage()))
                .path(request.getRequestURI())
                .build();



        response.setStatus(STATUS_VALUE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getOutputStream(), error);

        log.error("<AI service> AccessDenied Error: {}", error, accessDeniedException);
    }
}
