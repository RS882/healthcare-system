package com.healthcare.billing.payment.validation;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;

import java.util.Currency;

public class CashPaymentValidator {

    public void validate(Payment payment, Money receivedAmount) {

        validatePayment(payment);

        validateReceivedAmount(receivedAmount, payment);
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new CashPaymentValidationException("Payment cannot be null");
        }

        if (payment.getMethod() != PaymentMethod.CASH) {
            throw new CashPaymentValidationException("Payment method must be CASH for cash payment");
        }
    }

    private void validateReceivedAmount(Money receivedAmount, Payment payment) {

        if (receivedAmount == null) {
            throw new CashPaymentValidationException("Received amount cannot be null");
        }

        if (receivedAmount.isZero()) {
            throw new CashPaymentValidationException("Received amount cannot be zero");
        }

        if (receivedAmount.isNegative()) {
            throw new CashPaymentValidationException("Received amount cannot be negative");
        }

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
