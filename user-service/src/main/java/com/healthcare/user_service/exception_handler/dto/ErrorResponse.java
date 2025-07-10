package com.healthcare.user_service.exception_handler.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response with errors information ")
public class ErrorResponse {

    @Schema(description = "The time of the appearance of an error", example = "21.07.2025 11:20")
    private LocalDateTime timestamp;

    @Schema(description = "Error http status", example = "400")
    private int status;

    @Schema(description = "Description of status", example = "Bad request")
    private String error;

    @Schema(description = "Errors message", example = "[\"Error of validation\", \"Email is wrong\"]")
    private Set<String> message = new HashSet<>();

    @Schema(description = "The path when contacting which an error arose", example = "/api/v1/users/registration")
    private String path;

    @Schema(description = "Validation errors")
    @ArraySchema(schema = @Schema(implementation = ValidationError.class))
    private Set<ValidationError> validationErrors;
}
