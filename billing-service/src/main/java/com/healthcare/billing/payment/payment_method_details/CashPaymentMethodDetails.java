package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.validation.MoneyValidation;

public record CashPaymentMethodDetails(
        Money receivedAmount
) implements PaymentMethodDetails {

    public CashPaymentMethodDetails {

        MoneyValidation.validatePositive(
                receivedAmount,
                "Received amount",
                CashPaymentValidationException::new );
    }
}
