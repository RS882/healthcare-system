package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.PaymentValidationException;

public sealed interface PaymentCompletionCommand
        permits CashPaymentCompletionCommand,
        CardPaymentCompletionCommand,
        BankTransferCompletionCommand {

    static void validatePaymentId(Long paymentId) {
        if (paymentId == null) {
            throw new PaymentValidationException("Payment id cannot be null");
        }
        if (paymentId <= 0) {
            throw new PaymentValidationException("Payment id must be greater than zero");
        }
    }
}
