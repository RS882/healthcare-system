package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.PaymentBalance;

public interface PaymentBalanceCalculator {
    PaymentBalance calculateInvoicePaymentBalance(Money invoiceTotal, Money paymentTotal);
}
