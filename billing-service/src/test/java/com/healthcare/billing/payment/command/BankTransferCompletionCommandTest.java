package com.healthcare.billing.payment.command;

import com.healthcare.billing.exception.BankTransferValidationException;
import com.healthcare.billing.exception.PaymentValidationException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BankTransferCompletionCommandTest {

    @Test
    void should_return_bank_transfer_completion_command() {

        Long paymentId = 34L;
        String bankTransactionId = "  iw2199uansdaui&&y1         ";

        BankTransferCompletionCommand result =
                new BankTransferCompletionCommand(paymentId, bankTransactionId);

        assertNotNull(result);
        assertEquals(paymentId, result.paymentId());
        assertEquals(bankTransactionId, result.bankTransactionId());
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void should_return_exception_when_payment_id_is_incorrect(Long paymentId) {

        String bankTransactionId = "  iw2199uansdaui&&y1         ";

        assertThrows(PaymentValidationException.class,
                () -> new BankTransferCompletionCommand(
                        paymentId, bankTransactionId
                ));
    }


    @ParameterizedTest(name = "Test {index}: bank transaction id [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            "           "
    })
    void should_return_exception_when_bank_transaction_id_is_incorrect(String bankTransactionId) {

        Long paymentId = 34L;

        assertThrows(BankTransferValidationException.class,
                () -> new BankTransferCompletionCommand(
                        paymentId, bankTransactionId
                ));
    }

}