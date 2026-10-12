package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.BankTransferValidationException;
import org.springframework.util.StringUtils;

public record BankTransferCompletionCommand(
        Long paymentId,
        String bankTransactionId
) implements PaymentCompletionCommand {

    public BankTransferCompletionCommand {

        PaymentCompletionCommand.validatePaymentId(paymentId);
        validateBankTransactionId(bankTransactionId);
    }

    private static void validateBankTransactionId(String bankTransactionId) {

        if (!StringUtils.hasText(bankTransactionId)) {
            throw new BankTransferValidationException("Bank transaction id cannot be empty or null");
        }
    }
}
