package com.healthcare.aiservice.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error details")
public record ValidationError(

        @Schema(
                description = "Field name that failed validation",
                example = "email"
        )
        String field,

        @Schema(
                description = "Validation error message",
                example = "Email must be valid"
        )
        String message

) {
}