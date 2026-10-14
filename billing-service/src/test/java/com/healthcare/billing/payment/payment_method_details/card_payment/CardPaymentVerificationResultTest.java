package com.healthcare.billing.payment.payment_method_details.card_payment;

import com.healthcare.billing.exception.CardPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentVerificationStatus;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CardPaymentVerificationResultTest {

    private static final String ID = "  osiuy&8-tesy ";
    private static final CardPaymentVerificationStatus STATUS = CardPaymentVerificationStatus.PENDING;
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("34.91", EUR);


    @ParameterizedTest(name = "Test {index}: status [{arguments}]")
    @EnumSource(
            value = CardPaymentVerificationStatus.class,
            names = {"SUCCEEDED", "PENDING", "FAILED"}
    )
    void should_return_correct_result(CardPaymentVerificationStatus status) {

        CardPaymentVerificationResult result =
                new CardPaymentVerificationResult(ID, status, AMOUNT);

        assertNotNull(result);
        assertEquals(ID, result.providerTransactionId());
        assertEquals(status, result.status());
        assertEquals(AMOUNT, result.amount());
    }

    @ParameterizedTest(name = "Test {index}: provider transaction id [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            "        "
    })
    void should_return_exception_when_id_is_incorrect(String id) {

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentVerificationResult(id, STATUS, AMOUNT));
    }

    @Test
    void should_return_exception_when_status_is_null() {

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentVerificationResult(ID, null, AMOUNT));
    }

    @Test
    void should_return_exception_when_amount_is_null() {

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentVerificationResult(ID, STATUS, null));
    }

    @Test
    void should_return_exception_when_amount_is_zero() {

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentVerificationResult(ID, STATUS, Money.zero(EUR)));
    }

    @Test
    void should_return_exception_when_amount_is_negative() {

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentVerificationResult(ID, STATUS, Money.of("-37.9", EUR)));
    }
}