package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.payment.model.enums.PaymentMethod;

public sealed interface PaymentMethodDetails permits
        CashPaymentMethodDetails,
        CardPaymentMethodDetails {

    PaymentMethod  getPaymentMethod();
}
