package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class InvalidRolesConfigurationException extends RestException{

    public InvalidRolesConfigurationException() {
        super(HttpStatus.BAD_REQUEST, "Roles must not be null or empty");
    }
}
