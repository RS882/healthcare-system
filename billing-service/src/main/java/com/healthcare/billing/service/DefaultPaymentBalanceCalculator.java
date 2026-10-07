package com.healthcare.billing.service;

import com.healthcare.billing.exception.MoneyValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.PaymentBalance;
import com.healthcare.billing.service.interfaces.PaymentBalanceCalculator;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
public class DefaultPaymentBalanceCalculator implements PaymentBalanceCalculator {
    @Override
    public PaymentBalance calculateInvoicePaymentBalance(
            Money invoiceTotal,
            Money paymentTotal
    ) {
        validateAmount(invoiceTotal, AmountType.INVOICE_TOTAL);
        validateAmount(paymentTotal, AmountType.PAYMENT_TOTAL);

        return resolvePaymentBalance(
                invoiceTotal.subtract(paymentTotal)
        );
    }

    private PaymentBalance resolvePaymentBalance(Money balance) {

        Currency currency = balance.currency();
        Money balanceAmount = balance.abs();
        Money zero = Money.zero(currency);

        Money remainingAmount = balance.isPositive() ? balanceAmount : zero;
        Money overpaidAmount = balance.isNegative() ? balanceAmount : zero;

        return PaymentBalance.builder()
                .remainingAmount(remainingAmount)
                .overpaidAmount(overpaidAmount)
                .build();
    }

    private void validateAmount(Money amount, AmountType type) {

        if (amount == null) {
            throw new MoneyValidationException("%s is null".formatted(type.getTypeOfAmount()));
        }

        if (amount.isNegative()) {
            throw new MoneyValidationException("%s is negative".formatted(type.getTypeOfAmount()));
        }

        if (type == AmountType.INVOICE_TOTAL && amount.isZero()) {
            throw new MoneyValidationException("%s must be greater than zero".formatted(type.getTypeOfAmount()));

        }
    }

    @Getter
    private enum AmountType {

        INVOICE_TOTAL("Invoice total amount"),
        PAYMENT_TOTAL("Paid amount");
        private final String typeOfAmount;

        AmountType(String typeOfAmount) {
            this.typeOfAmount = typeOfAmount;
        }
    }
}


