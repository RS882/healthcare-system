package com.healthcare.billing.payment.model;

import com.healthcare.billing.payment.model.enums.PaymentStatus;

import java.time.Instant;

public class PendingPaymentState extends AbstractPaymentState{

    @Override
    public void complete(Payment payment, Instant eventTime) {

        validatePayment(payment);

        payment.applyStatus(PaymentStatus.COMPLETED, eventTime);
    }

    @Override
    public void cancel(Payment payment, Instant eventTime) {

        validatePayment(payment);

        payment.applyStatus(PaymentStatus.CANCELLED, eventTime);
    }
}
