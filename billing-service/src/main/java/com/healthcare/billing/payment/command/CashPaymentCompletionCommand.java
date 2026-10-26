package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.PaymentBalanceValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.validation.MoneyValidation;

public record CashPaymentCompletionCommand(
        Long paymentId,
        Money receivedAmount
) implements PaymentCompletionCommand {

    public CashPaymentCompletionCommand {

        PaymentCompletionCommand.validatePaymentId(paymentId);
        validateAmount(receivedAmount);
    }

    private static void validateAmount(Money receivedAmount) {

        MoneyValidation.validatePositive(
                receivedAmount,
                "Received amount",
                CashPaymentValidationException::new);
    }
}
