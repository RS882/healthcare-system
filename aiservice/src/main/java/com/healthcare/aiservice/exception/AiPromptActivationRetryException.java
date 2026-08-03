package com.healthcare.aiservice.exception;

public class AiPromptActivationRetryException
        extends RuntimeException {

    public AiPromptActivationRetryException(
            String promptId,
            Throwable cause
    ) {
        super(
                "Transient conflict while activating AI prompt '%s'"
                        .formatted(promptId),
                cause
        );
    }
}