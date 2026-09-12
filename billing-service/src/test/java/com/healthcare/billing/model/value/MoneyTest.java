package com.healthcare.billing.model.value;

import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.MoneyValidationException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void shouldCreateMoney() {
        BigDecimal value = new BigDecimal("100");

        Money money = Money.of(value, EUR);

        assertEquals(0, money.amount().compareTo(value));
        assertEquals(EUR, money.currency());
    }

    @Test
    void shouldCreateMoneyFromStringAndStripInput() {
        Money money = Money.of(" 10.50 ", EUR);

        assertEquals(0, money.amount().compareTo(new BigDecimal("10.50")));
        assertEquals(EUR, money.currency());
    }

    @Test
    void shouldConsiderAmountsWithDifferentScaleEqual() {
        Money first = Money.of(new BigDecimal("10.0"), EUR);
        Money second = Money.of(new BigDecimal("10.00"), EUR);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotConsiderDifferentCurrenciesEqual() {
        Money euro = Money.of(new BigDecimal("10.00"), EUR);
        Money dollar = Money.of(new BigDecimal("10.00"), USD);

        assertNotEquals(euro, dollar);
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money first = Money.of(new BigDecimal("10.50"), EUR);
        Money second = Money.of(new BigDecimal("5.25"), EUR);

        Money result = first.add(second);

        assertEquals(0, result.amount().compareTo(new BigDecimal("15.75")));
        assertEquals(EUR, result.currency());
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        Money first = Money.of(new BigDecimal("10.50"), EUR);
        Money second = Money.of(new BigDecimal("5.25"), EUR);

        Money result = first.subtract(second);

        assertEquals(0, result.amount().compareTo(new BigDecimal("5.25")));
        assertEquals(EUR, result.currency());
    }

    @Test
    void shouldRejectAdditionWithDifferentCurrencies() {
        Money euro = Money.of(new BigDecimal("10.00"), EUR);
        Money dollar = Money.of(new BigDecimal("5.00"), USD);

        CurrencyMismatchException exception =
                assertThrows(CurrencyMismatchException.class, () -> euro.add(dollar));

        assertEquals("Currency mismatch: expected EUR, but was USD", exception.getMessage());
    }

    @Test
    void shouldRejectSubtractionWithDifferentCurrencies() {
        Money euro = Money.of(new BigDecimal("10.00"), EUR);
        Money dollar = Money.of(new BigDecimal("5.00"), USD);

        assertThrows(CurrencyMismatchException.class, () -> euro.subtract(dollar));
    }

    @Test
    void shouldRejectNullOperand() {
        Money euro = Money.of(new BigDecimal("10.00"), EUR);

        MoneyValidationException exception =
                assertThrows(MoneyValidationException.class, () -> euro.add(null));

        assertEquals("Money must not be null", exception.getMessage());
    }

    @Test
    void shouldDetectZeroAmount() {
        Money money = Money.zero(EUR);

        assertTrue(money.isZero());
        assertFalse(money.isPositive());
        assertFalse(money.isNegative());
    }

    @Test
    void shouldDetectPositiveAmount() {
        Money money = Money.of(new BigDecimal("10.00"), EUR);

        assertTrue(money.isPositive());
        assertFalse(money.isZero());
        assertFalse(money.isNegative());
    }

    @Test
    void shouldDetectNegativeAmount() {
        Money money = Money.of(new BigDecimal("-10.00"), EUR);

        assertTrue(money.isNegative());
        assertFalse(money.isZero());
        assertFalse(money.isPositive());
    }

    @Test
    void shouldRejectNullAmount() {
        MoneyValidationException exception = assertThrows(
                MoneyValidationException.class,
                () -> Money.of((BigDecimal) null, EUR)
        );

        assertEquals("Amount must not be null", exception.getMessage());
    }

    @Test
    void shouldRejectNullCurrency() {
        MoneyValidationException exception = assertThrows(
                MoneyValidationException.class,
                () -> Money.of(new BigDecimal("10.00"), null)
        );

        assertEquals("Currency must not be null", exception.getMessage());
    }

    @Test
    void shouldRejectNullStringAmount() {
        assertThrows(
                MoneyValidationException.class,
                () -> Money.of((String) null, EUR)
        );
    }

    @Test
    void shouldRejectBlankStringAmount() {
        assertThrows(
                MoneyValidationException.class,
                () -> Money.of("   ", EUR)
        );
    }

    @Test
    void shouldRejectInvalidStringAmount() {
        MoneyValidationException exception = assertThrows(
                MoneyValidationException.class,
                () -> Money.of("abc", EUR)
        );

        assertEquals("Invalid money amount: 'abc'", exception.getMessage());
    }

    @Test
    void should_treat_amounts_with_different_scale_as_numerically_equal() {
        BigDecimal first = new BigDecimal("10.0");
        BigDecimal second = new BigDecimal("10.00");

        assertThat(first.equals(second)).isFalse();
        assertThat(first.compareTo(second)).isZero();
    }
}
