package com.healthcare.billing.model.value;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

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

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> euro.add(dollar));

        assertEquals("Currencies must be the same", exception.getMessage());
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
        assertThrows(
                NullPointerException.class,
                () -> Money.of((BigDecimal) null, EUR)
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThrows(
                NullPointerException.class,
                () -> Money.of(
                        new BigDecimal("10.00"),
                        null
                )
        );
    }
}