package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;

public record CashPaymentCompletionCommand(
        Long paymentId,
        Money receivedAmount
) implements PaymentCompletionCommand {

    public CashPaymentCompletionCommand {
        validateId(paymentId);
        validateAmount(receivedAmount);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new PaymentValidationException("Payment id cannot be null");
        }
        if (id <= 0) {
            throw new PaymentValidationException("Payment id must be greater than zero");
        }
    }

    private static void validateAmount(Money receivedAmount) {
        if (receivedAmount == null) {
            throw new CashPaymentValidationException("Received amount cannot be null");
        }

        if (receivedAmount.isNegative() || receivedAmount.isZero()) {
            throw new CashPaymentValidationException("Received amount must be positive");
        }
    }
}
