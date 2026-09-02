package com.healthcare.billing.validation;


import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class BillingValidation {

    private BillingValidation() {
    }

    public static void validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }

    public static void validateQuantity(BigDecimal quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quantity must not be null"
            );
        }

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    public static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new IllegalArgumentException(
                    "Unit price must not be null"
            );
        }

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Unit price must not be negative"
            );
        }
    }

    public static void validateTaxRate(BigDecimal taxRate) {
        if (taxRate == null) {
            throw new IllegalArgumentException(
                    "Tax rate must not be null"
            );
        }

        if (taxRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "Tax rate must not be negative"
            );
        }

        if (taxRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Tax rate must not be greater than 1"
            );
        }
    }
}
