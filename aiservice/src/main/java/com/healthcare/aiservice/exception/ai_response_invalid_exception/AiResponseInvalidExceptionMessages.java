package com.healthcare.aiservice.exception.ai_response_invalid_exception;

public final class AiResponseInvalidExceptionMessages {
    private AiResponseInvalidExceptionMessages() {
    }

    public static final String MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE = "AI response does not match medical information extraction schema";
    public static final String MEDICAL_SUMMARY_EXCEPTION_MESSAGE = "AI response does not match expected medical summary schema";
    public static final String CLASSIFICATION_EXCEPTION_MESSAGE = "AI response does not match message classification schema";
}
