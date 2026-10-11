package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.PaymentCompletionResult;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;

public interface PaymentCompletionService {

    PaymentCompletionResult<? extends PaymentMethodDetails> completePayment(
            PaymentCompletionCommand command
    );
}
