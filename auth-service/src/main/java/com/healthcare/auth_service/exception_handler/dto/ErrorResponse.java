package com.healthcare.auth_service.exception_handler.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response with errors information ")
public class ErrorResponse {

    @Schema(description = "The time of the appearance of an error", example = "21.07.2025 11:20")
    private Instant timestamp;

    @Schema(description = "Error http status", example = "401")
    private int status;

    @Schema(description = "Description of status", example = "Unauthorized")
    private String error;

    @Schema(description = "Errors message", example = "[\"Error of validation\", \"Email is null\"]")
    private List<String> message = new ArrayList<>();

    @Schema(description = "The path when contacting which an error arose", example = "/api/v1/auth/login")
    private String path;

    @Schema(description = "Validation errors")
    @ArraySchema(schema = @Schema(implementation = ValidationError.class))
    private Set<ValidationError> validationErrors;
}
