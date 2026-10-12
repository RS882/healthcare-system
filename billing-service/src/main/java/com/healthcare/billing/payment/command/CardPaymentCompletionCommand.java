package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CardPaymentValidationException;
import org.springframework.util.StringUtils;

public record CardPaymentCompletionCommand(
        Long paymentId,
        String providerTransactionId
) implements PaymentCompletionCommand {

    public CardPaymentCompletionCommand {

        PaymentCompletionCommand.validatePaymentId(paymentId);
        validateProviderTransactionId(providerTransactionId);
    }

    private static void validateProviderTransactionId(String providerTransactionId) {

        if (!StringUtils.hasText(providerTransactionId)) {
            throw new CardPaymentValidationException("Provider transaction id cannot be empty or null");
        }
    }
}
