package com.healthcare.billing.validation;


import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class BillingValidation {

    private BillingValidation() {
    }

    public static void validateId(Long id) {
        if (id == null ) {
            throw new IllegalArgumentException("Id must not be null");
        }

        if ( id < 1) {
            throw new IllegalArgumentException("Id must not be positive");
        }
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

    public static void validateDiscountRate(BigDecimal discountRate) {
        if (discountRate == null) {
            throw new IllegalArgumentException(
                    "Discount rate must not be null"
            );
        }

        if (discountRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "Discount rate must not be negative"
            );
        }

        if (discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Discount rate must not be greater than 1"
            );
        }
    }
}
