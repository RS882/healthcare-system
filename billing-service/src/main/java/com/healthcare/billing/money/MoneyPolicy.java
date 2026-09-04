package com.healthcare.billing.money;


import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.model.value.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Component
public class MoneyPolicy {

    private static final RoundingMode ROUNDING_MODE =
            RoundingMode.HALF_UP;

    private final Currency currency;

    public MoneyPolicy(BillingProperties properties) {
        this.currency = Currency.getInstance(
                properties.currency()
        );
    }

    public Money moneyOf(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Money amount must not be null"
            );
        }

        BigDecimal roundedAmount = amount.setScale(
                currency.getDefaultFractionDigits(),
                ROUNDING_MODE
        );

        return Money.of(roundedAmount, currency);
    }

    public Money zero() {
        return moneyOf(BigDecimal.ZERO);
    }

    public Currency currency() {
        return currency;
    }
}