package com.healthcare.billing.config.annotation.validate_id;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IdValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidId {

    String idName() default "id";

    String message() default "Id is invalid";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
