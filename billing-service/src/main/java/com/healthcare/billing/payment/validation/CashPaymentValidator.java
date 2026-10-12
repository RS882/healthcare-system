package com.healthcare.billing.payment.validation;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CashPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;

import java.util.Currency;
import java.util.Objects;



public final class CashPaymentValidator {

    private CashPaymentValidator() {
    }

    public static void validate(Payment payment, CashPaymentCompletionCommand command) {

        validateCommand(command);

        validatePayment(payment);

        if(!Objects.equals(payment.getId(), command.paymentId())) {
            throw new CashPaymentValidationException("Payment id mismatch");
        }

        validateReceivedAmount(command.receivedAmount(), payment);
    }

    private static void validateCommand(CashPaymentCompletionCommand command){
        if(command == null){
            throw new CashPaymentValidationException("Cash payment completion command is null");
        }
    }

    private static void validatePayment(Payment payment) {
        if (payment == null) {
            throw new CashPaymentValidationException("Payment cannot be null");
        }

        if (payment.getMethod() != PaymentMethod.CASH) {
            throw new CashPaymentValidationException("Payment method must be CASH for cash payment");
        }
    }

    private static void validateReceivedAmount(Money receivedAmount, Payment payment) {

        Currency receivedAmountCurrency = receivedAmount.currency();
        Currency paymentCurrency = payment.getAmount().currency();

        if (!receivedAmountCurrency.equals(paymentCurrency)) {
            throw new CashPaymentValidationException(
                    "Received amount currency <%s> does not match payment currency <%s>"
                            .formatted(
                                    receivedAmountCurrency.getCurrencyCode(),
                                    paymentCurrency.getCurrencyCode()
                            )
            );
        }

        Money paymentAmount = payment.getAmount();

        if (receivedAmount.compareTo(paymentAmount) < 0) {
            throw new CashPaymentValidationException(
                    "Received amount <%s> is less than payment amount <%s>"
                            .formatted(receivedAmount, paymentAmount)
            );
        }
    }
}
