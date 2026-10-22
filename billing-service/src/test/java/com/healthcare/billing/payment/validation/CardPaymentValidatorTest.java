package com.healthcare.billing.payment.validation;

import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CardPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CardPaymentValidatorTest {

    private static final Long INVOICE_ID = 12L;
    private static final Long PAYMENT_ID = 2L;
    private static final String PROVIDER_TRANSACTION_ID = "Test provider transaction ID  ";
    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.STRIPE;
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", EUR);

    private static final PaymentMethod METHOD = PaymentMethod.CARD;

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private final CardPaymentValidator validator = new CardPaymentValidator();

    @Test
    void validation_must_be_successful() {

        assertDoesNotThrow(() ->
                validator.validate(
                        createValidPayment(),
                        createValidCommand()
                ));
    }

    @Test
    void should_return_exception_when_command_is_null() {

        assertThrows(PaymentValidationException.class,
                () -> validator.validate(
                        createValidPayment(),
                        null
                ));
    }

    @Test
    void should_return_exception_when_payment_is_null() {

        assertThrows(PaymentValidationException.class,
                () -> validator.validate(
                        null,
                        createValidCommand()
                ));
    }

    @Test
    void should_return_exception_when_payment_method_is_not_card() {

        Payment payment = Payment.reconstitute(
                PAYMENT_ID,
                INVOICE_ID,
                AMOUNT,
                PaymentMethod.CASH,
                PaymentStatus.CREATED,
                FIXED_CLOCK.instant(),
                null,
                null
        );

        assertThrows(PaymentValidationException.class,
                () -> validator.validate(
                        payment,
                        createValidCommand()
                ));
    }

    @Test
    void should_return_exception_when_payment_ids_do_not_match() {

        CardPaymentCompletionCommand command = new CardPaymentCompletionCommand(
                999L,
                PROVIDER_TRANSACTION_ID,
                PROVIDER
        );

        assertThrows(
                PaymentValidationException.class,
                () -> validator.validate(
                        createValidPayment(),
                        command
                )
        );
    }

    private Payment createValidPayment() {
        return Payment.reconstitute(
                PAYMENT_ID,
                INVOICE_ID,
                AMOUNT,
                METHOD,
                PaymentStatus.CREATED,
                FIXED_CLOCK.instant(),
                null,
                null
        );
    }

    private CardPaymentCompletionCommand createValidCommand() {
        return new CardPaymentCompletionCommand(PAYMENT_ID, PROVIDER_TRANSACTION_ID, PROVIDER);
    }
}