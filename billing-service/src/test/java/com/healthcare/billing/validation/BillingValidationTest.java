package com.healthcare.billing.validation;

import com.healthcare.billing.exception.BillingValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingValidationTest {

    @Test
    void shouldAcceptValidValues() {
        assertDoesNotThrow(() -> BillingValidation.validateId(1L));
        assertDoesNotThrow(() -> BillingValidation.validateDescription("Consultation"));
        assertDoesNotThrow(() -> BillingValidation.validateQuantity(BigDecimal.ONE));
        assertDoesNotThrow(() -> BillingValidation.validateUnitPrice(BigDecimal.ZERO));
        assertDoesNotThrow(() -> BillingValidation.validateDiscountRate(BigDecimal.ZERO));
        assertDoesNotThrow(() -> BillingValidation.validateDiscountRate(BigDecimal.ONE));
        assertDoesNotThrow(() -> BillingValidation.validateTaxRate(BigDecimal.ZERO));
        assertDoesNotThrow(() -> BillingValidation.validateTaxRate(BigDecimal.ONE));
    }

    @Test
    void shouldRejectInvalidId() {
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateId(null));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateId(0L));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateId(-1L));
    }

    @Test
    void shouldRejectInvalidDescription() {
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateDescription(null));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateDescription("   "));
    }

    @Test
    void shouldRejectInvalidQuantity() {
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateQuantity(null));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateQuantity(BigDecimal.ZERO));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateQuantity(BigDecimal.ONE.negate()));
    }

    @Test
    void shouldRejectInvalidUnitPrice() {
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateUnitPrice(null));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateUnitPrice(new BigDecimal("-0.01")));
    }

    @Test
    void shouldRejectInvalidDiscountRate() {
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateDiscountRate(null));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateDiscountRate(new BigDecimal("-0.01")));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateDiscountRate(new BigDecimal("1.01")));
    }

    @Test
    void shouldRejectInvalidTaxRate() {
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateTaxRate(null));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateTaxRate(new BigDecimal("-0.01")));
        assertThrows(BillingValidationException.class, () -> BillingValidation.validateTaxRate(new BigDecimal("1.01")));
    }
}
