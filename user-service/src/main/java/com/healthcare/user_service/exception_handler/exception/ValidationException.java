package com.healthcare.user_service.exception_handler.exception;


import com.healthcare.user_service.exception_handler.dto.ValidationError;
import org.springframework.http.HttpStatus;

import java.util.Set;

public class ValidationException extends RestException {

    public ValidationException(String message, Set<ValidationError> errorSet) {
        super(HttpStatus.BAD_REQUEST, message, errorSet);
    }
}
