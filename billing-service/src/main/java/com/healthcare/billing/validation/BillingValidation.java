package com.healthcare.billing.validation;

import com.healthcare.billing.exception.BillingValidationException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class BillingValidation {

    private BillingValidation() {
    }

    public static void validateId(Long id) {
        if (id == null) {
            throw new BillingValidationException("Id must not be null");
        }

        if (id < 1) {
            throw new BillingValidationException("Id must be greater than zero");
        }
    }

    public static void validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new BillingValidationException("Description cannot be empty");
        }
    }

    public static void validateQuantity(BigDecimal quantity) {
        if (quantity == null) {
            throw new BillingValidationException("Quantity must not be null");
        }

        if (quantity.signum() <= 0) {
            throw new BillingValidationException("Quantity must be greater than zero");
        }
    }

    public static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new BillingValidationException("Unit price must not be null");
        }

        if (unitPrice.signum() < 0) {
            throw new BillingValidationException("Unit price must not be negative");
        }
    }

    public static void validateTaxRate(BigDecimal taxRate) {
        if (taxRate == null) {
            throw new BillingValidationException("Tax rate must not be null");
        }

        if (taxRate.signum() < 0) {
            throw new BillingValidationException("Tax rate must not be negative");
        }

        if (taxRate.compareTo(BigDecimal.ONE) > 0) {
            throw new BillingValidationException("Tax rate must not be greater than 1");
        }
    }

    public static void validateDiscountRate(BigDecimal discountRate) {
        if (discountRate == null) {
            throw new BillingValidationException("Discount rate must not be null");
        }

        if (discountRate.signum() < 0) {
            throw new BillingValidationException("Discount rate must not be negative");
        }

        if (discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new BillingValidationException("Discount rate must not be greater than 1");
        }
    }
}