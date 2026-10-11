package com.healthcare.billing.payment.command;

public sealed interface PaymentCompletionCommand
        permits CashPaymentCompletionCommand {
}
