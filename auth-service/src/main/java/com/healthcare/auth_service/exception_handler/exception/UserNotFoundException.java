package com.healthcare.auth_service.exception_handler.exception;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long id, Throwable cause) {
        super("User with id <" + id + "> not found", cause);
    }

    public UserNotFoundException(Long id) {
        this(id, null);
    }

    public UserNotFoundException(String email, Throwable cause) {
        super("User with email <" + email + "> not found", cause);
    }

    public UserNotFoundException(String email) {
        this(email, null);
    }

    public UserNotFoundException(Throwable cause) {
        super("User not found", cause);
    }

}
