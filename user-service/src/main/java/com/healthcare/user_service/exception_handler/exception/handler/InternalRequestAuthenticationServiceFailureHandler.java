package com.healthcare.user_service.exception_handler.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.exception_handler.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InternalRequestAuthenticationServiceFailureHandler {

    private final ObjectMapper objectMapper;

    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws IOException {

        HttpStatus status =
                HttpStatus.SERVICE_UNAVAILABLE;

        ErrorResponse errorResponse =
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(List.of(
                                "Internal request authentication service is unavailable"
                        ))
                        .path(request.getRequestURI())
                        .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
