package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashPaymentCompletionCommandTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void should_return_cash_payment_completion_command() {

        Long paymentId = 34L;
        Money receivedAmount = Money.of("234.56", EUR);

        CashPaymentCompletionCommand result =
                new CashPaymentCompletionCommand(paymentId, receivedAmount);

        assertNotNull(result);
        assertEquals(paymentId, result.paymentId());
        assertEquals(receivedAmount, result.receivedAmount());
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void should_return_exception_when_payment_id_is_incorrect(Long paymentId) {

        Money receivedAmount = Money.of("234.56", EUR);

        assertThrows(PaymentValidationException.class,
                () -> new CashPaymentCompletionCommand(
                        paymentId, receivedAmount
                ));
    }

    @Test
    void should_return_exception_when_received_amount_is_null() {

        Long paymentId = 34L;

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentCompletionCommand(
                        paymentId, null
                ));
    }

    @Test
    void should_return_exception_when_received_amount_is_zero() {

        Long paymentId = 34L;
        Money receivedAmount = Money.zero(EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentCompletionCommand(
                        paymentId, receivedAmount
                ));
    }

    @Test
    void should_return_exception_when_received_amount_is_negative() {

        Long paymentId = 34L;
        Money receivedAmount = Money.of("-234.56", EUR);

        assertThrows(CashPaymentValidationException.class,
                () -> new CashPaymentCompletionCommand(
                        paymentId, receivedAmount
                ));
    }
}