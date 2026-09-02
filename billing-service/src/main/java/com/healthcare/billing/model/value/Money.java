package com.healthcare.billing.model.value;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class Money {

    private final BigDecimal amount;

    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {

        this.amount = Objects.requireNonNull(
                amount,
                "Amount must not be null"
        );
        this.currency = Objects.requireNonNull(
                currency,
                "Currency must not be null"
        );
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, Currency currency) {
        return new Money( new BigDecimal(amount), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public Money add(Money other) {

        requireSameCurrency(other);

        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);

        return new Money(
                amount.subtract(other.amount),
                currency
        );
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(
                other,
                "Money must not be null"
        );

        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currencies must be the same"
            );
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Money other)) {
            return false;
        }

        return amount.compareTo(other.amount) == 0
                && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                amount.stripTrailingZeros(),
                currency
        );
    }

    @Override
    public String toString() {
        return amount.toPlainString()
                + " "
                + currency.getCurrencyCode();
    }
}
