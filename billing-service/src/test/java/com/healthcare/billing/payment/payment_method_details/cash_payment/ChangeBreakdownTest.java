package com.healthcare.billing.payment.payment_method_details.cash_payment;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ChangeBreakdownTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void should_return_change_breakdown() {

        Money change = Money.of("25.82", EUR);

        Map<String, Long> denominations = getDenominations();

        ChangeBreakdown changeBreakdown =
                new ChangeBreakdown(change, denominations);

        assertNotNull(changeBreakdown);
        assertEquals(change, changeBreakdown.change());
        assertEquals(denominations, changeBreakdown.denominations());
    }

    @Test
    void should_allow_zero_change() {

        Money change = Money.zero(EUR);

        Map<String, Long> denominations = new LinkedHashMap<>();

        ChangeBreakdown changeBreakdown =
                new ChangeBreakdown(change, denominations);

        assertNotNull(changeBreakdown);
        assertEquals(Money.zero(EUR), changeBreakdown.change());
        assertTrue(changeBreakdown.denominations().isEmpty());
    }

    @Test
    void should_defensively_copy_denominations() {

        Money change = Money.of("25.82", EUR);

        Map<String, Long> denominations = getDenominations();

        ChangeBreakdown changeBreakdown =
                new ChangeBreakdown(change, denominations);

        denominations.put("100 EUR", 1L);

        assertNotNull(changeBreakdown);
        assertEquals(change, changeBreakdown.change());
        assertNotEquals(denominations, changeBreakdown.denominations());
        assertNull(changeBreakdown.denominations().get("100 EUR"));
    }

    @Test
    void should_return_exception_when_change_amount_is_null() {

        assertThrows(CashPaymentValidationException.class,
                () -> new ChangeBreakdown(null, getDenominations()));
    }

    @Test
    void should_return_exception_when_change_amount_is_negative() {

        Money change = Money.of("-25.82", EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new ChangeBreakdown(change, getDenominations()));
    }

    @Test
    void should_return_exception_when_denominations_is_null() {

        Money change = Money.of("25.82", EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new ChangeBreakdown(change, null));
    }

    @Test
    void should_throw_exception_when_modifying_immutable_map() {

        Money change = Money.of("25.82", EUR);

        Map<String, Long> denominations = getDenominations();

        ChangeBreakdown changeBreakdown =
                new ChangeBreakdown(change, denominations);

        Map<String, Long> changeBreakdownDenominations = changeBreakdown.denominations();

        assertThrows(UnsupportedOperationException.class,
                () -> changeBreakdownDenominations.put("100 EUR", 1L));
    }

    @Test
    void should_preserve_denominations_insertion_order() {

        Money change = Money.of("25.82", EUR);

        Map<String, Long> denominations = getDenominations();

        ChangeBreakdown result = new ChangeBreakdown(
                change,
                denominations
        );

        assertEquals(
                new ArrayList<>(denominations.keySet()),
                new ArrayList<>(result.denominations().keySet())
        );
    }

    private Map<String, Long> getDenominations() {

        Map<String, Long> denominations = new LinkedHashMap<>();

        denominations.put("20 EUR", 1L);
        denominations.put("5 EUR", 1L);
        denominations.put("50 cent", 1L);
        denominations.put("20 cent", 1L);
        denominations.put("10 cent", 1L);
        denominations.put("2 cent", 1L);

        return denominations;
    }
}