package com.healthcare.billing.money;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyPolicyTest {

    private MoneyPolicy moneyPolicy;

    @BeforeEach
    void setUp() {
        BillingProperties properties = new BillingProperties("EUR", 7);

        moneyPolicy = new MoneyPolicy(properties);
    }

    @Test
    void shouldUseConfiguredCurrency() {
        assertEquals(
                Currency.getInstance("EUR"),
                moneyPolicy.currency()
        );
    }

    @Test
    void shouldRoundMoneyHalfUp() {
        Money money = moneyPolicy.moneyOf(new BigDecimal("10.125"));

        assertEquals(
                0,
                money.amount()
                        .compareTo(new BigDecimal("10.13"))
        );
    }

    @Test
    void shouldRoundMoneyDownWhenThirdDigitIsLessThanFive() {
        Money money = moneyPolicy.moneyOf(                new BigDecimal("10.124")        );

        assertEquals(
                0,
                money.amount()
                        .compareTo(new BigDecimal("10.12"))
        );
    }

    @Test
    void shouldRoundMoneyUpWhenThirdDigitIsGreaterThanFive() {
        Money money = moneyPolicy.moneyOf(                new BigDecimal("10.126")        );

        assertEquals(
                0,
                money.amount()
                        .compareTo(new BigDecimal("10.13"))
        );
    }

    @Test
    void shouldCreateZeroMoneyWithCurrencyScale() {
        Money money = moneyPolicy.zero();

        assertEquals(
                new BigDecimal("0.00"),
                money.amount()
        );

        assertEquals(
                Currency.getInstance("EUR"),
                money.currency()
        );
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> moneyPolicy.moneyOf(null)
        );
    }

}