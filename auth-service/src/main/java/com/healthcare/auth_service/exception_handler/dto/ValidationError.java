package com.healthcare.auth_service.exception_handler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Errors of validation")
public class ValidationError {

    @Schema(description = "The field where the error arose", example = "name")
    private String field;

    @Schema(description = "Error message", example = "Name cannot be null")
    private String message;
}
