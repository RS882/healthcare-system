package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.validation.IdValidation;

public sealed interface PaymentCompletionCommand
        permits CashPaymentCompletionCommand,
        CardPaymentCompletionCommand,
        BankTransferCompletionCommand {

    static void validatePaymentId(Long paymentId) {

        IdValidation.validateId(paymentId, "Payment id", PaymentValidationException::new);

    }
}
