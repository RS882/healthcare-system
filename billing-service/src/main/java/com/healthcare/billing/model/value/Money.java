package com.healthcare.billing.model.value;

import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.MoneyValidationException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        validateAmount(amount);
        validateCurrency(currency);

        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, Currency currency) {
        if (!StringUtils.hasText(amount)) {
            throw new MoneyValidationException("Amount must not be null or blank");
        }

        try {
            return new Money(new BigDecimal(amount.strip()), currency);
        } catch (NumberFormatException exception) {
            throw new MoneyValidationException("Invalid money amount: '%s'".formatted(amount));
        }
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

        return new Money(amount.subtract(other.amount), currency);
    }

    private void requireSameCurrency(Money other) {
        if (other == null) {
            throw new MoneyValidationException("Money must not be null");
        }

        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new MoneyValidationException("Amount must not be null");
        }
    }

    private static void validateCurrency(Currency currency) {
        if (currency == null) {
            throw new MoneyValidationException("Currency must not be null");
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

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);

        return amount.compareTo(other.amount);
    }
}