package com.healthcare.billing.payment.validation;

import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;

public interface PaymentValidator<T extends PaymentCompletionCommand> {

    void validate(Payment payment, T command);
}
