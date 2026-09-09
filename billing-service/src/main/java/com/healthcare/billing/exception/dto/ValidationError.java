package com.healthcare.billing.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error details")
public record ValidationError(

        @Schema(
                description = "Field name that failed validation",
                example = "Id"
        )
        String field,

        @Schema(
                description = "Validation error message",
                example = "Id must be valid"
        )
        String message

) {
}