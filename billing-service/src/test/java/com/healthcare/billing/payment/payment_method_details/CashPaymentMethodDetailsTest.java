package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;


@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashPaymentMethodDetailsTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void should_return_cash_payment_method_details() {

        Money receivedAmount = Money.of("100.00", EUR);

        CashPaymentMethodDetails result = new CashPaymentMethodDetails(receivedAmount);

        assertNotNull(result);
        assertEquals(receivedAmount, result.receivedAmount());

    }

    @ParameterizedTest(name = "Test {index}: amount [{arguments}]")
    @ValueSource(strings = {
            "0",
            "-172.59"
    })
    void should_return_exception_when_received_amount_is_incorrect(String amount) {

        Money receivedAmount = Money.of(amount, EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentMethodDetails(receivedAmount));
    }

    @Test
    void should_return_exception_when_received_amount_is_null() {

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentMethodDetails(null));
    }

}