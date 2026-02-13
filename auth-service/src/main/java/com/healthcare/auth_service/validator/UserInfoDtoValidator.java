package com.healthcare.auth_service.validator;

import com.healthcare.auth_service.domain.dto.UserAuthDto;
import com.healthcare.auth_service.exception_handler.dto.ValidationError;
import com.healthcare.auth_service.exception_handler.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserInfoDtoValidator {

    public final Validator validator;

    public void validateUser(UserAuthDto dto) {
        Set<ConstraintViolation<UserAuthDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {

            Set<ValidationError> errors = violations.stream()
                    .map(v -> new ValidationError(v.getPropertyPath().toString(), v.getMessage()))
                    .collect(Collectors.toSet());

            throw new ValidationException("Invalid UserInfoDto received: ", errors);
        }
    }
}
