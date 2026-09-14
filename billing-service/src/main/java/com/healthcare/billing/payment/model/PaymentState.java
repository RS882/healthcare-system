package com.healthcare.billing.payment.model;

import java.time.Instant;

public interface PaymentState {

    void start(Payment payment, Instant eventTime);

    void complete(Payment payment, Instant eventTime);

    void cancel(Payment payment, Instant eventTime);
}
