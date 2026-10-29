package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CardPaymentValidationException;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import org.springframework.util.StringUtils;

public record CardPaymentMethodDetails(
        String providerTransactionId,
        CardPaymentProvider provider
) implements PaymentMethodDetails {
    public CardPaymentMethodDetails {

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

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }
}
