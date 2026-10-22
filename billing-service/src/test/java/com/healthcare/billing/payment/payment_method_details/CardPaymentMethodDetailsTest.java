package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.CardPaymentValidationException;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CardPaymentMethodDetailsTest {

    @ParameterizedTest(name = "Test {index}: provider transaction id [{arguments}]")
    @ValueSource(strings = {
            "iuYgbs7#2",
            "   90Hyuhsn(*^   "
    })
    void should_return_card_payment_method_details(String id) {

        CardPaymentMethodDetails details = new CardPaymentMethodDetails(id, CardPaymentProvider.STRIPE);

        assertNotNull(details);
        assertEquals(id, details.providerTransactionId());
    }

    @ParameterizedTest(name = "Test {index}: provider transaction id [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            "        "
    })
    void should_return_exception_when_id_is_incorrect(String id) {

        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentMethodDetails(id, CardPaymentProvider.STRIPE));
    }

    @Test
    void should_return_exception_when_provider_is_null() {
        assertThrows(CardPaymentValidationException.class,
                () -> new CardPaymentMethodDetails("Uyyds^7", null));
    }
}