package com.healthcare.billing.payment.resolver;


import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;

public interface PaymentProcessorDispatcher {

    PaymentMethodDetails process(
            Payment payment,
            PaymentCompletionCommand command
    );
}
