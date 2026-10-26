package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;

public record CashPaymentMethodDetails(
        Money receivedAmount
) implements PaymentMethodDetails {

    public CashPaymentMethodDetails {

        validatePaymentMethodDetails(receivedAmount);

    }

    private static void validatePaymentMethodDetails(Money receivedAmount) {

        if (receivedAmount == null) {
            throw new CashPaymentValidationException("Received amount cannot be null");
        }

        if (!receivedAmount.isPositive()) {
            throw new CashPaymentValidationException("Received amount must be positive");
        }
    }
}
