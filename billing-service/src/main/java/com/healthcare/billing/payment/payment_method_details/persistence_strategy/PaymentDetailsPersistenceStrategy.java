package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;

import java.util.Optional;

public interface PaymentDetailsPersistenceStrategy {

    PaymentMethod supportedMethod();

    void save(
            PaymentMethodDetails details,
            Long paymentId
    );

    Optional<PaymentMethodDetails> findByPaymentId(
            Long paymentId
    );
}
