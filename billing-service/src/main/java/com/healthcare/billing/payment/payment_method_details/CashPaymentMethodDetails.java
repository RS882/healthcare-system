package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.ChangeBreakdown;

import java.util.Currency;

public record CashPaymentMethodDetails(
        Money receivedAmount,
        ChangeBreakdown changeBreakdown
) implements PaymentMethodDetails {

    public CashPaymentMethodDetails {

        validatePaymentMethodDetails(receivedAmount);
        validateChangeBreakdown(changeBreakdown, receivedAmount.currency());
    }

    private static void validatePaymentMethodDetails(Money receivedAmount) {

        if (receivedAmount == null) {
            throw new CashPaymentValidationException("Received amount cannot be null");
        }

        if (!receivedAmount.isPositive()) {
            throw new CashPaymentValidationException("Received amount must be positive");
        }
    }

    private static void validateChangeBreakdown(
            ChangeBreakdown changeBreakdown,
            Currency receivedAmountCurrency
    ) {
        if (changeBreakdown == null) {
            throw new CashPaymentValidationException("Received change breakdown cannot be null");
        }

        Currency changeBreakdownCurrency = changeBreakdown.change().currency();
        if (!receivedAmountCurrency.equals(changeBreakdownCurrency)) {
            throw new CurrencyMismatchException(receivedAmountCurrency, changeBreakdownCurrency);
        }
    }
}
