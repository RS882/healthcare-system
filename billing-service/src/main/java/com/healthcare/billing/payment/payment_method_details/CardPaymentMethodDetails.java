package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CardPaymentValidationException;
import org.springframework.util.StringUtils;

public record CardPaymentMethodDetails(
        String providerTransactionId
) implements PaymentMethodDetails {
    public CardPaymentMethodDetails {

        validateProviderTransactionId(providerTransactionId);
    }

    private static void validateProviderTransactionId(String providerTransactionId) {

        if (!StringUtils.hasText(providerTransactionId)) {
            throw new CardPaymentValidationException("Provider transaction id cannot be empty or null");
        }
    }
}
