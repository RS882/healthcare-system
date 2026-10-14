package com.healthcare.billing.payment.payment_method_details.card_payment;

import com.healthcare.billing.exception.CardPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentVerificationStatus;
import org.springframework.util.StringUtils;

public record CardPaymentVerificationResult(
        String providerTransactionId,
        CardPaymentVerificationStatus status,
        Money amount
) {

    public CardPaymentVerificationResult {

        validateProviderTransactionId(providerTransactionId);
        validateStatus(status);
        validateAmount(amount);
    }

    private static void validateProviderTransactionId(String providerTransactionId) {

        if (!StringUtils.hasText(providerTransactionId)) {
            throw new CardPaymentValidationException("Provider transaction id cannot be empty or null");
        }
    }

    private static void validateStatus(CardPaymentVerificationStatus status) {
        if (status == null) {
            throw new CardPaymentValidationException("Card payment verification status cannot be null");
        }
    }

    private static void validateAmount(Money amount) {
        if (amount == null) {
            throw new CardPaymentValidationException("Amount cannot be null");
        }
        if (!amount.isPositive()) {
            throw new CardPaymentValidationException("Amount cannot be negative or zero");
        }
    }
}
