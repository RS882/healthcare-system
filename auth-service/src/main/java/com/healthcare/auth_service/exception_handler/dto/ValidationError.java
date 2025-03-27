package com.healthcare.auth_service.exception_handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ValidationError {
    private String field;
    private String message;
}
