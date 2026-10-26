package com.healthcare.billing.validation;


import com.healthcare.billing.exception.RestException;

import java.util.Objects;
import java.util.function.Function;

public final class IdValidation {

    private IdValidation() {
    }

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

            String errorMessage = "%s is invalid.".formatted(capitalizeName(idName));

            throw exceptionFactory.apply(errorMessage);
        }
    }

    private static String capitalizeName(String name) {

        String defaultName = "Id";

        if (name == null || name.isBlank()) {
            return defaultName;
        }

        String stripName = name.strip();

        return stripName.substring(0, 1).toUpperCase() + stripName.substring(1);
    }

}
