package com.healthcare.billing.payment.payment_method_details.card_payment;

import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;

public interface CardPaymentGateway {

    CardPaymentVerificationResult verifyTransaction (
            String providerTransactionId,
            CardPaymentProvider provider);
}
