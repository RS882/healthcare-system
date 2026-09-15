package com.healthcare.billing.payment.payment_details;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.model.Payment;
import lombok.Getter;

@Getter
public abstract class PaymentDetails {

    protected final Payment payment;

    protected PaymentDetails(Payment payment) {

        validatePayment(payment);

        this.payment = payment;
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new PaymentDetailsValidationException("Payment cannot be null");
        }
    }
}
