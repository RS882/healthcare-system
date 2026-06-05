package com.healthcare.aiservice.exception.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Schema(description = "Error response")
@Builder
public record ErrorResponse(

        @Schema(
                description = "Timestamp when the error occurred",
                example = "2026-06-01T13:45:00Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,

        @Schema(
                description = "Error code",
                example = "BAD_REQUEST"
        )
        String error,

        @Schema(
                description = "Human-readable error message",
                example = "Validation failed"
        )
        String message,

        @Schema(
                description = "Request path",
                example = "/api/v1/ai/medical-note/summary"
        )
        String path,

        @ArraySchema(
                schema = @Schema(implementation = ValidationError.class)
        )
        Set<ValidationError> validationErrors

) {
}