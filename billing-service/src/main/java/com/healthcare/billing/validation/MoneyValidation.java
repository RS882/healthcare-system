package com.healthcare.billing.validation;

import com.healthcare.billing.exception.RestException;
import com.healthcare.billing.model.value.Money;

import java.util.Objects;
import java.util.function.Function;

import static com.healthcare.billing.validation.ValidationNameFormatter.capitalizeName;

public final class MoneyValidation {

    private MoneyValidation() {
    }

    private static final String DEFAULT_NAME = "Amount";

    public static void validateNonNegative(
            Money amount,
            String amountName,
            Function<String, ? extends RestException> exceptionFactory
    ) {

        if (amount == null) {
            throw createException(amountName, exceptionFactory, "cannot be null");
        }

        if (amount.isNegative()) {
            throw createException(amountName, exceptionFactory, "cannot be negative");
        }
    }

    public static void validatePositive(
            Money amount,
            String amountName,
            Function<String, ? extends RestException> exceptionFactory
    ) {

        validateNonNegative(amount, amountName, exceptionFactory);

        if (amount.isZero()) {
            throw createException(amountName, exceptionFactory, "cannot be zero");
        }
    }

    private static RestException createException(
            String amountName,
            Function<String, ? extends RestException> exceptionFactory,
            String reason
    ) {
        Objects.requireNonNull(
                exceptionFactory,
                "Exception factory must not be null"
        );

        String message = "%s %s".formatted(
                capitalizeName(amountName, DEFAULT_NAME),
                reason
        );

        return exceptionFactory.apply(message);
    }
}
