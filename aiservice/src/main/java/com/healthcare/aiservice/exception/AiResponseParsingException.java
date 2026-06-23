package com.healthcare.aiservice.exception;


public class AiResponseParsingException extends RuntimeException {

    private final String rawResponse;
    private final String extractedJson;

    public AiResponseParsingException(
            String message,
            String rawResponse,
            String extractedJson,
            Throwable cause
    ) {
        super(message, cause);
        this.rawResponse = rawResponse;
        this.extractedJson = extractedJson;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public String getExtractedJson() {
        return extractedJson;
    }
}
