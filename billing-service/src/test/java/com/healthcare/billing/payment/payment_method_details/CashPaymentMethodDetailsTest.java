package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.cash_payment.ChangeBreakdown;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashPaymentMethodDetailsTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void should_return_cash_payment_method_details() {

        Money receivedAmount = Money.of("100.00", EUR);
        ChangeBreakdown changeBreakdown = getChangeBreakdown();

        CashPaymentMethodDetails result = new CashPaymentMethodDetails(
                receivedAmount,
                changeBreakdown
        );

        assertNotNull(result);
        assertEquals(receivedAmount, result.receivedAmount());
        assertEquals(changeBreakdown, result.changeBreakdown());
    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "0",
            "-172.59"
    })
    void should_return_exception_when_received_amount_is_incorrect(String amount) {

        Money receivedAmount = Money.of(amount, EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentMethodDetails(receivedAmount, getChangeBreakdown()));
    }

    @Test
    void should_return_exception_when_received_amount_is_null() {

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentMethodDetails(null, getChangeBreakdown()));
    }

    @Test
    void should_return_exception_when_change_breakdown_is_null() {

        Money receivedAmount = Money.of("25.82", EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentMethodDetails(receivedAmount, null));
    }

    @Test
    void should_return_exception_when_currencies_do_not_match() {

        Money receivedAmount = Money.of("25.82", USD);

        assertThrows(CurrencyMismatchException.class,
                () -> new CashPaymentMethodDetails(receivedAmount, getChangeBreakdown()));
    }

    private ChangeBreakdown getChangeBreakdown() {

        Money change = Money.of("25.82", EUR);

        Map<String, Long> denominations = new LinkedHashMap<>();

        denominations.put("20 EUR", 1L);
        denominations.put("5 EUR", 1L);
        denominations.put("50 cent", 1L);
        denominations.put("20 cent", 1L);
        denominations.put("10 cent", 1L);
        denominations.put("2 cent", 1L);

        return new ChangeBreakdown(change, denominations);
    }
}