package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CardPaymentValidationException;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import org.springframework.util.StringUtils;

public record CardPaymentCompletionCommand(
        Long paymentId,
        String providerTransactionId,
        CardPaymentProvider provider
) implements PaymentCompletionCommand {

    public CardPaymentCompletionCommand {

        PaymentCompletionCommand.validatePaymentId(paymentId);
        validateProviderTransactionId(providerTransactionId);
        validateProvider(provider);
    }

    private static void validateProviderTransactionId(String providerTransactionId) {

        if (!StringUtils.hasText(providerTransactionId)) {
            throw new CardPaymentValidationException("Provider transaction id cannot be empty or null");
        }
    }

    private static void validateProvider(CardPaymentProvider provider) {
        if (provider == null) {
            throw new CardPaymentValidationException("Provider cannot be null");
        }
    }
}
