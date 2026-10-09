package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.PaymentBalanceValidationException;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment balance tests: ")
class PaymentBalanceTest {

    private  static final Currency CURRENCY_EUR = Currency.getInstance("EUR");
    private static final Currency CURRENCY_USD = Currency.getInstance("USD");

    @ParameterizedTest(name = "Test {index}:  amounts [{arguments}]")
    @CsvSource({
            "100.12, 0",
            "0,   0",
            "0, 250.92"
    })
    void should_create_payment_balance(String remainingAmount, String overpaidAmount) {

        Money remainingMoney = Money.of(remainingAmount, CURRENCY_EUR);
        Money overpaidMoney = Money.of(overpaidAmount, CURRENCY_EUR);

        PaymentBalance balance = PaymentBalance.builder()
                .remainingAmount(remainingMoney)
                .overpaidAmount(overpaidMoney)
                .build();

        assertNotNull(balance);
        assertEquals(remainingMoney, balance.remainingAmount());
        assertEquals(overpaidMoney, balance.overpaidAmount());
    }

    @Test
    void should_return_exception_when_remaining_amount_is_null() {

        Money overpaidMoney = Money.zero(CURRENCY_EUR);

        assertThrows(PaymentBalanceValidationException.class, () -> PaymentBalance.builder()
                .remainingAmount(null)
                .overpaidAmount(overpaidMoney)
                .build());
    }

    @Test
    void should_return_exception_when_remaining_amount_is_negative() {

        Money remainingMoney = Money.of("-282.98", CURRENCY_EUR);
        Money overpaidMoney = Money.zero(CURRENCY_EUR);

        assertThrows(PaymentBalanceValidationException.class, () -> PaymentBalance.builder()
                .remainingAmount(remainingMoney)
                .overpaidAmount(overpaidMoney)
                .build());
    }

    @Test
    void should_return_exception_when_overpaid_amount_is_null() {

        Money remainingMoney = Money.zero(CURRENCY_EUR);

        assertThrows(PaymentBalanceValidationException.class, () -> PaymentBalance.builder()
                .remainingAmount(remainingMoney)
                .overpaidAmount(null)
                .build());
    }

    @Test
    void should_return_exception_when_overpaid_amount_is_negative() {

        Money overpaidMoney = Money.of("-282.98", CURRENCY_EUR);
        Money remainingMoney = Money.zero(CURRENCY_EUR);

        assertThrows(PaymentBalanceValidationException.class, () -> PaymentBalance.builder()
                .remainingAmount(remainingMoney)
                .overpaidAmount(overpaidMoney)
                .build());
    }

    @Test
    void should_return_exception_when_currencies_do_not_match() {

        Money remainingMoney = Money.of("32.10", CURRENCY_EUR);
        Money overpaidMoney = Money.zero(CURRENCY_USD);

        assertThrows(PaymentBalanceValidationException.class, () -> PaymentBalance.builder()
                .remainingAmount(remainingMoney)
                .overpaidAmount(overpaidMoney)
                .build());
    }

    @Test
    void should_reject_payment_balance_when_both_amounts_are_positive() {

        Money remainingMoney = Money.of("32.10", CURRENCY_EUR);
        Money overpaidMoney = Money.of("546.91", CURRENCY_EUR);

        assertThrows(PaymentBalanceValidationException.class, () -> PaymentBalance.builder()
                .remainingAmount(remainingMoney)
                .overpaidAmount(overpaidMoney)
                .build());
    }

    @Test
    void should_reject_payment_balance_when_both_amounts_are_positive_and_use_constructor() {

        Money remainingMoney = Money.of("32.10", CURRENCY_EUR);
        Money overpaidMoney = Money.of("546.91", CURRENCY_EUR);

        assertThrows(PaymentBalanceValidationException.class,
                () -> new PaymentBalance(remainingMoney, overpaidMoney));
    }
}