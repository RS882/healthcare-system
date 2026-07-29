package com.healthcare.aiservice.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Validation
    VALIDATION_ERROR("Validation failed"),
    INVALID_REQUEST_PARAMETER("Invalid request parameter"),
    INVALID_REQUEST_BODY("Request body is invalid"),

    // AI Provider
    AI_PROVIDER_UNAVAILABLE("AI provider is unavailable"),
    AI_PROVIDER_ERROR("AI provider failed to process the request"),
    AI_PROVIDER_TIMEOUT("AI provider timeout"),

    // AI Response
    AI_RESPONSE_PARSING_ERROR("AI provider returned invalid response format"),
    AI_RESPONSE_INVALID("AI provider returned invalid response"),
    AI_RESPONSE_UNEXPECTED("AI returned unexpected response"),

    // Prompt Management
    AI_PROMPT_NOT_FOUND("AI prompt not found"),
    AI_PROMPT_VERSION_CONFLICT("Prompt version conflict"),
    AI_PROMPT_STATE_INVALID("Invalid prompt state"),

    // Common
    INTERNAL_SERVER_ERROR("Unexpected internal server error");

    private final String defaultMessage;
}
