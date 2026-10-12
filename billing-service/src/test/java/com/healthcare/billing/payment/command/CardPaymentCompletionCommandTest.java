package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.CardPaymentValidationException;
import com.healthcare.billing.exception.PaymentValidationException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CardPaymentCompletionCommandTest {

    @Test
    void should_return_card_payment_completion_command() {

        Long paymentId = 34L;
        String providerTransactionId = "  iw2199uansdaui&&y1         ";

        CardPaymentCompletionCommand result =
                new CardPaymentCompletionCommand(paymentId, providerTransactionId);

        assertNotNull(result);
        assertEquals(paymentId, result.paymentId());
        assertEquals(providerTransactionId, result.providerTransactionId());
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void should_return_exception_when_payment_id_is_incorrect(Long paymentId) {

        String providerTransactionId = "  iw2199uansdaui&&y1         ";

        assertThrows(PaymentValidationException.class,
                () -> new CardPaymentCompletionCommand(
                        paymentId, providerTransactionId
                ));
    }


    @ParameterizedTest(name = "Test {index}: provider transaction id [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            "           "
    })
    void should_return_exception_when_provider_transaction_id_is_incorrect(String providerTransactionId) {

        Long paymentId = 34L;

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentCompletionCommand(
                        paymentId, providerTransactionId
                ));
    }
}