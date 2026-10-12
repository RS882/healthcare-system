package com.healthcare.billing.payment.processor;

import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;

public interface PaymentProcessor<
        C extends PaymentCompletionCommand,
        D extends PaymentMethodDetails> {

    D process(Payment payment, C command);
}