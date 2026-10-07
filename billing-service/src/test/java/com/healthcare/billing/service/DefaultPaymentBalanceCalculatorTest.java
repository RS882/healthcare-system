package com.healthcare.billing.service;

import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.MoneyValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.PaymentBalance;
import com.healthcare.billing.service.interfaces.PaymentBalanceCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default payment balance calculator tests: ")
class DefaultPaymentBalanceCalculatorTest {

    private final PaymentBalanceCalculator calculator = new DefaultPaymentBalanceCalculator();

    private static final Currency CURRENCY_EUR = Currency.getInstance("EUR");
    private static final Currency CURRENCY_USD = Currency.getInstance("USD");

    @Test
    void should_calculate_balance_when_invoice_is_partially_paid() {

        String invoiceAmount = "328.29";
        String paidAmount = "200.1";

        Money balanceMoney = Money.of("128.19", CURRENCY_EUR);

        Money invoiceMoney = Money.of(invoiceAmount, CURRENCY_EUR);
        Money paidMoney = Money.of(paidAmount, CURRENCY_EUR);

        PaymentBalance result = calculator.calculateInvoicePaymentBalance(invoiceMoney, paidMoney);

        assertNotNull(result);
        assertNotNull(result.remainingAmount());
        assertNotNull(result.overpaidAmount());

        assertEquals(result.remainingAmount(), balanceMoney);
        assertEquals(result.overpaidAmount(), Money.zero(CURRENCY_EUR));
    }

    @Test
    void should_calculate_balance_when_invoice_has_no_payments() {

        String invoiceAmount = "328.29";

        Money invoiceMoney = Money.of(invoiceAmount, CURRENCY_EUR);
        Money paidMoney = Money.zero(CURRENCY_EUR);

        PaymentBalance result = calculator.calculateInvoicePaymentBalance(invoiceMoney, paidMoney);

        assertNotNull(result);
        assertNotNull(result.remainingAmount());
        assertNotNull(result.overpaidAmount());

        assertEquals(result.remainingAmount(), invoiceMoney);
        assertEquals(result.overpaidAmount(), Money.zero(CURRENCY_EUR));
    }

    @Test
    void should_calculate_balance_when_invoice_is_fully_paid() {

        String amount = "328.29";

        Money moneyAmount = Money.of(amount, CURRENCY_EUR);
        Money zero = Money.zero(CURRENCY_EUR);

        PaymentBalance result = calculator.calculateInvoicePaymentBalance(moneyAmount, moneyAmount);

        assertNotNull(result);
        assertNotNull(result.remainingAmount());
        assertNotNull(result.overpaidAmount());

        assertEquals(result.remainingAmount(), zero);
        assertEquals(result.overpaidAmount(), zero);
    }

    @Test
    void should_calculate_balance_when_invoice_is_overpaid() {

        String invoiceAmount = "58.29";
        String paidAmount = "165.76";

        Money balanceMoney = Money.of("107.47", CURRENCY_EUR);

        Money invoiceMoney = Money.of(invoiceAmount, CURRENCY_EUR);
        Money paidMoney = Money.of(paidAmount, CURRENCY_EUR);

        PaymentBalance result = calculator.calculateInvoicePaymentBalance(invoiceMoney, paidMoney);

        assertNotNull(result);
        assertNotNull(result.remainingAmount());
        assertNotNull(result.overpaidAmount());

        assertEquals(result.remainingAmount(), Money.zero(CURRENCY_EUR));
        assertEquals(result.overpaidAmount(), balanceMoney);
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "-29817.9800 ",
            " 0"
    })
    void should_reject_invoice_total_when_not_positive(String amount) {

        String paidAmount = "165.76";

        Money invoiceMoney = Money.of(amount, CURRENCY_EUR);
        Money paidMoney = Money.of(paidAmount, CURRENCY_EUR);

        assertThrows(MoneyValidationException.class,
                () -> calculator.calculateInvoicePaymentBalance(invoiceMoney, paidMoney));
    }

    @Test
    void should_return_exception_when_invoice_total_is_null() {

        String paidAmount = "165.76";

        Money paidMoney = Money.of(paidAmount, CURRENCY_EUR);

        assertThrows(MoneyValidationException.class,
                () -> calculator.calculateInvoicePaymentBalance(null, paidMoney));
    }

    @Test
    void should_reject_negative_payment_total() {

        String invoiceAmount = "458.29";
        String paidAmount = "-165.76";

        Money invoiceMoney = Money.of(invoiceAmount, CURRENCY_EUR);
        Money paidMoney = Money.of(paidAmount, CURRENCY_EUR);

        assertThrows(MoneyValidationException.class,
                () -> calculator.calculateInvoicePaymentBalance(invoiceMoney, paidMoney));
    }

    @Test
    void should_return_exception_when_payment_total_is_null() {

        String invoiceAmount = "165.76";

        Money invoiceMoney = Money.of(invoiceAmount, CURRENCY_EUR);

        assertThrows(MoneyValidationException.class,
                () -> calculator.calculateInvoicePaymentBalance(invoiceMoney, null));
    }

    @Test
    void should_return_exception_when_currencies_do_not_match() {

        String invoiceAmount = "328.29";
        String paidAmount = "200.1";

        Money invoiceMoney = Money.of(invoiceAmount, CURRENCY_EUR);
        Money paidMoney = Money.of(paidAmount, CURRENCY_USD);

        assertThrows(CurrencyMismatchException.class,
                () -> calculator.calculateInvoicePaymentBalance(invoiceMoney, paidMoney));
    }
}