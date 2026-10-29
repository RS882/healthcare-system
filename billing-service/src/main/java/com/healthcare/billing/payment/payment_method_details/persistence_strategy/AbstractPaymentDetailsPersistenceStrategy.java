package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.validation.IdValidation;

public abstract class AbstractPaymentDetailsPersistenceStrategy
        implements PaymentDetailsPersistenceStrategy {

    protected void validateId(Long paymentId) {
        IdValidation.validateId(
                paymentId,
                "Payment id",
                PaymentDetailsValidationException::new
        );
    }

    protected void validateDetails(PaymentMethodDetails details) {
        if (details == null) {
            throw new PaymentDetailsValidationException(
                    "Payment method details is null"
            );
        }
    }
}