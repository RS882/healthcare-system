package com.healthcare.aiservice.exception.ai_response_invalid_exception;

import com.healthcare.aiservice.exception.RestException;
import org.springframework.http.HttpStatus;

public class AiResponseInvalidException extends RestException {

    public AiResponseInvalidException( String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
