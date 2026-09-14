package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.PaymentStateMethodException;
import com.healthcare.billing.exception.PaymentValidationException;

import java.time.Instant;

public abstract class AbstractPaymentState implements PaymentState {

    @Override
    public void start(Payment payment, Instant eventTime) {

        validatePayment(payment);

        throw new PaymentStateMethodException("start", payment.getStatus());
    }

    @Override
    public void complete(Payment payment, Instant eventTime) {
        validatePayment(payment);

        throw new PaymentStateMethodException("complete", payment.getStatus());
    }

    @Override
    public void cancel(Payment payment, Instant eventTime) {

        validatePayment(payment);

        throw new PaymentStateMethodException("cancel", payment.getStatus());
    }

    protected void validatePayment(Payment payment) {
        if (payment == null) {
            throw new PaymentValidationException("Payment cannot be null");
        }
    }
}
