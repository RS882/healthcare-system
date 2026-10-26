package com.healthcare.billing.config.annotation.validate_id;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdValidator implements ConstraintValidator<ValidId, Long> {

    private String idName;

    @Override
    public void initialize(ValidId constraintAnnotation) {
        this.idName = constraintAnnotation.idName();
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {

        if (value != null && value > 0) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        String errorMessage = String.format("Identifier '%s' is invalid. Provided value: %s", idName, value);

        context.buildConstraintViolationWithTemplate(errorMessage)
                .addConstraintViolation();

        return false;
    }
}
