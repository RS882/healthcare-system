package com.healthcare.billing.payment.payment_method_details.card_payment;

import com.healthcare.billing.exception.CardPaymentGatewayException;
import com.healthcare.billing.exception.CardPaymentServerUnavailableException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentVerificationStatus;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StubCardPaymentGatewayTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private final StubCardPaymentGateway gateway = new StubCardPaymentGateway();


    @ParameterizedTest(name = "Test {index}: transaction data[{arguments}]")
    @MethodSource("transactionData")
    void should_return_card_payment_verification_result(
            String id,
            CardPaymentVerificationStatus status,
            Money amount) {

        CardPaymentVerificationResult result = gateway.verifyTransaction(id);

        assertNotNull(result);
        assertEquals(id, result.providerTransactionId());
        assertEquals(status, result.status());
        assertEquals(amount, result.amount());

    }

    @ParameterizedTest(name = "Test {index}: provider transaction id [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            "        "
    })
    void should_return_exception_when_provider_transaction_id_is_incorrect(String providerTransactionId) {

        assertThrows(CardPaymentGatewayException.class,
                () -> gateway.verifyTransaction(providerTransactionId));
    }


    @Test
    void should_return_exception_when_transaction_not_found() {

        String providerTransactionId = "UNKNOWN_TRANSACTION_ID";

        assertThrows(CardPaymentGatewayException.class,
                () -> gateway.verifyTransaction(providerTransactionId));

    }

    @Test
    void should_return_exception_when_payment_server_is_unavailable() {

        String providerTransactionId = "ERROR_ID#4";

        assertThrows(CardPaymentServerUnavailableException.class,
                () -> gateway.verifyTransaction(providerTransactionId));

    }

    private static Stream<Arguments> transactionData() {
        return Stream.of(
                Arguments.of(
                        "SUCCEEDED_ID#1",
                        CardPaymentVerificationStatus.SUCCEEDED,
                        Money.of("256.91", EUR)
                ),
                Arguments.of(
                        "PENDING_ID#2",
                        CardPaymentVerificationStatus.PENDING,
                        Money.of("10.5", EUR)
                ),
                Arguments.of(
                        "FAILED_ID#3",
                        CardPaymentVerificationStatus.FAILED,
                        Money.of("870.28", EUR)
                )
        );
    }
}
