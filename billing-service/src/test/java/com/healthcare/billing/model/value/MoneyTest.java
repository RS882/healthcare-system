package com.healthcare.billing.model.value;

import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.MoneyValidationException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "129",
            "61.7",
            "-4737.98",
            "988.9000000",
            "-981772.5100000000000"
    })
    void shouldCreateMoney(String amount) {
        BigDecimal value = new BigDecimal(amount);

        Money money = Money.of(value, EUR);

        assertEquals(0, money.amount().compareTo(value));
        assertEquals(EUR, money.currency());
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "129",
            "746.0000000000000000"
    })
    void shouldCreateMoneyWhenCurrencyIsJPY(String amount) {
        BigDecimal value = new BigDecimal(amount);

        Money money = Money.of(value, JPY);

        assertEquals(0, money.amount().compareTo(value));
        assertEquals(JPY, money.currency());
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "129 ",
            " 746.0000000000000000"
    })
    void shouldCreateMoneyFromStringAndStripInputWhenCurrencyIsJPY(String amount) {
        Money money = Money.of(amount, JPY);

        assertEquals(0, money.amount().compareTo(new BigDecimal(amount.strip())));
        assertEquals(JPY, money.currency());
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "129",
            " 10.50 ",
            " 61.7",
            "-4737.98",
            "988.9000000 ",
            " -981772.5100000000000"
    })
    void shouldCreateMoneyFromStringAndStripInput(String amount) {
        Money money = Money.of(amount, EUR);

        assertEquals(0, money.amount().compareTo(new BigDecimal(amount.strip())));
        assertEquals(EUR, money.currency());
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "584.872",
            "584.8710",
            "-123.456"
    })
    void should_reject_amount_with_too_many_fractional_digits(String amount) {
        BigDecimal value = new BigDecimal(amount);

        assertThrows(MoneyValidationException.class, () -> Money.of(value, EUR));
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "584.872",
            "584.8710",
            "-123.456",
            "61.7",
            "-4737.98",
            "988.9000000",
            "-981772.5100000000000"
    })
    void should_reject_amount_with_too_many_fractional_digits_when_currency_JPY(String amount) {
        BigDecimal value = new BigDecimal(amount);

        assertThrows(MoneyValidationException.class, () -> Money.of(value, JPY));
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "584.872",
            "584.8710",
            "-123.456",
            "61.7",
            "-4737.98",
            "988.9000000",
            "-981772.5100000000000"
    })
    void should_reject_string_amount_with_too_many_fractional_digits_when_currency_JPY(String amount) {

        assertThrows(MoneyValidationException.class, () -> Money.of(amount, JPY));
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "584.872",
            " 584.8710",
            "-123.456"
    })
    void should_reject_string_amount_with_too_many_fractional_digits(String amount) {

        assertThrows(MoneyValidationException.class, () -> Money.of(amount, EUR));
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

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "29817.9800 ",
            " 0",
            "-9287.8 "
    })
    void should_return_absolute_amount(String amount) {
        Money money = Money.of(amount, EUR);

        Money absMoney = money.abs();

        assertNotNull(absMoney);
        assertEquals(money.currency(), absMoney.currency());

        if(money.isNegative()) {
            assertEquals(Money.zero(EUR), absMoney.add(money));
        }

        if(money.isPositive()) {
            assertEquals(0, money.compareTo(absMoney));
        }

        if(money.isZero()) {
            assertEquals(Money.zero(EUR), absMoney);
        }
    }
}
