package com.healthcare.billing.validation;


import com.healthcare.billing.exception.RestException;

import java.util.Objects;
import java.util.function.Function;

import static com.healthcare.billing.validation.ValidationNameFormatter.capitalizeName;

public final class IdValidation  {

    private IdValidation() {
    }

    private static final String DEFAULT_NAME = "Id";

    public static boolean isValid(Long id) {

        return id != null && id > 0;
    }

    public static void validateId(
            Long id,
            String idName,
            Function<String, ? extends RestException> exceptionFactory
    ) {

        if (!IdValidation.isValid(id)) {

            Objects.requireNonNull(exceptionFactory, "Exception factory must not be null");

            String errorMessage = "%s is invalid.".formatted(
                    capitalizeName(idName, DEFAULT_NAME)
            );

            throw exceptionFactory.apply(errorMessage);
        }
    }
}
