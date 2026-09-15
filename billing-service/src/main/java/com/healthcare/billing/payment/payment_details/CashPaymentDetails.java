package com.healthcare.billing.payment.payment_details;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.ChangeBreakdown;
import lombok.Getter;

@Getter
public class CashPaymentDetails extends PaymentDetails {

    private final Money receivedAmount;

    private final ChangeBreakdown changeBreakdown;

    public CashPaymentDetails(Payment payment,
                              Money receivedAmount,
                              ChangeBreakdown changeBreakdown) {
        super(payment);

        validatePaymentMethod();

        validateReceivedAmount(receivedAmount);
        validateChangeBreakdown(changeBreakdown);

        this.receivedAmount = receivedAmount;
        this.changeBreakdown = changeBreakdown;

    }

    private void validatePaymentMethod() {
        if (this.payment.getMethod() != PaymentMethod.CASH) {
            throw new PaymentDetailsValidationException("Payment method must be CASH for cash payment");
        }
    }

    private void validateChangeBreakdown(ChangeBreakdown changeBreakdown) {
        if (changeBreakdown == null) {
            throw new PaymentDetailsValidationException("Change breakdown cannot be null");
        }
    }

    private void validateReceivedAmount(Money receivedAmount) {

        if (receivedAmount == null) {
            throw new PaymentDetailsValidationException("Received amount cannot be null");
        }

        if (receivedAmount.isZero()) {
            throw new PaymentDetailsValidationException("Received amount cannot be zero");
        }

        if (receivedAmount.isNegative()) {
            throw new PaymentDetailsValidationException("Received amount cannot be negative");
        }

        if (!receivedAmount.currency().equals(payment.getAmount().currency())) {
            throw new PaymentDetailsValidationException("Received amount does not match currency");
        }
    }
}
