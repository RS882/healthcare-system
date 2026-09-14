package com.healthcare.billing.payment.model;

import com.healthcare.billing.payment.model.enums.PaymentStatus;

import java.time.Instant;

public class CreatedPaymentState extends AbstractPaymentState {

    @Override
    public void start(Payment payment, Instant eventTime) {

        validatePayment(payment);

        payment.applyStatus(PaymentStatus.PENDING, eventTime);
    }

    @Override
    public void cancel(Payment payment, Instant eventTime) {

        validatePayment(payment);

        payment.applyStatus(PaymentStatus.CANCELLED, eventTime);
    }
}
