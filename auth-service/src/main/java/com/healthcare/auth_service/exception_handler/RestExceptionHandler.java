package com.healthcare.auth_service.exception_handler;

import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import com.healthcare.auth_service.exception_handler.dto.ValidationError;
import com.healthcare.auth_service.exception_handler.exception.RestException;
import com.healthcare.auth_service.exception_handler.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidation(MethodArgumentNotValidException ex) {
        Set<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> ValidationError.builder()
                        .field(err.getField())
                        .message(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toSet());

        throw new ValidationException("The error of validation of the request", validationErrors);
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleException(RestException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ex.getResponse();

        errorResponse.setPath(request.getRequestURI());
        log.error("REST Error: {}", errorResponse, ex);

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }


}
