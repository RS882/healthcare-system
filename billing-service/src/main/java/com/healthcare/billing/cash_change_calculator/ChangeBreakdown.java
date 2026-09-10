package com.healthcare.billing.cash_change_calculator;

import com.healthcare.billing.model.value.Money;

import java.util.Map;

public record ChangeBreakdown(
        Money change,
        Map<String, Long> denominations
) {
}
