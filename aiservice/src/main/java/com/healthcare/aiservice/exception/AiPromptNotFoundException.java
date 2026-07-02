package com.healthcare.aiservice.exception;

import org.springframework.http.HttpStatus;

public class AiPromptNotFoundException extends RestException {
    public AiPromptNotFoundException(String promptId) {

        super(HttpStatus.NOT_FOUND, String.format("AI prompt with id: %s no found", promptId));
    }
}
