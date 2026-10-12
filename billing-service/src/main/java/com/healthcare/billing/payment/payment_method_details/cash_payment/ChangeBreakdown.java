package com.healthcare.billing.payment.payment_method_details.cash_payment;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ChangeBreakdown(
        Money change,
        Map<String, Long> denominations
) {
    public ChangeBreakdown {

        validateChange(change);
        validateDenominations(denominations);

        denominations = Collections.unmodifiableMap(
                new LinkedHashMap<>(denominations)
        );
    }

    private static void validateChange(Money change) {
        if (change == null) {
            throw new CashPaymentValidationException("Change amount cannot be null");
        }

        if (change.isNegative()) {
            throw new CashPaymentValidationException("Change amount cannot be negative");
        }
    }

    private static void validateDenominations(Map<String, Long> denominations) {
        if (denominations == null) {
            throw new CashPaymentValidationException("Denominations cannot be null");
        }
    }
}
