package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;

public record CashPaymentCompletionCommand(
        Long paymentId,
        Money receivedAmount
) implements PaymentCompletionCommand {

    public CashPaymentCompletionCommand {

        PaymentCompletionCommand.validatePaymentId(paymentId);
        validateAmount(receivedAmount);
    }

    private static void validateAmount(Money receivedAmount) {
        if (receivedAmount == null) {
            throw new CashPaymentValidationException("Received amount cannot be null");
        }

        if (!receivedAmount.isPositive()) {
            throw new CashPaymentValidationException("Received amount must be positive");
        }
    }
}
