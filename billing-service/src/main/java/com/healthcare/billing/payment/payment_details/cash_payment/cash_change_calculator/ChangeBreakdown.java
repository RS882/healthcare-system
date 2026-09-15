package com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator;

import com.healthcare.billing.model.value.Money;

import java.util.Map;

public record ChangeBreakdown(
        Money change,
        Map<String, Long> denominations
) {
}
