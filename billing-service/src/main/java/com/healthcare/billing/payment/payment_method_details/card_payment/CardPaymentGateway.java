package com.healthcare.billing.payment.payment_method_details.card_payment;

public interface CardPaymentGateway {

    CardPaymentVerificationResult verifyTransaction (String providerTransactionId);
}
