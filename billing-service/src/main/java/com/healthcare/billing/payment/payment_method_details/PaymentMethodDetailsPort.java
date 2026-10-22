package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.payment.model.enums.PaymentMethod;

import java.util.Optional;

public interface PaymentMethodDetailsPort {

    void savePaymentMethodDetails(
            Long paymentId,
            PaymentMethodDetails details
    );

    Optional<PaymentMethodDetails> getPaymentMethodDetails(
            Long paymentId,
            PaymentMethod method);
}
