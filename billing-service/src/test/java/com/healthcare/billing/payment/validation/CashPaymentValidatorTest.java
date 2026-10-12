package com.healthcare.billing.payment.validation;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CashPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
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
class CashPaymentValidatorTest {


    private static final Long INVOICE_ID = 12L;
    private static final Long PAYMENT_ID = 2L;
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Money AMOUNT = Money.of("328.29", EUR);

    private static final PaymentMethod METHOD = PaymentMethod.CASH;

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @Test
    void validation_must_be_successful() {

        assertDoesNotThrow(() ->
                CashPaymentValidator.validate(
                        createValidPayment(),
                        createValidCommand()
                ));
    }

    @Test
    void should_validate_when_received_amount_equals_payment_amount() {

        CashPaymentCompletionCommand command = new CashPaymentCompletionCommand(
                PAYMENT_ID,
                AMOUNT
        );

        assertDoesNotThrow(
                () -> CashPaymentValidator.validate(
                        createValidPayment(),
                        command
                )
        );
    }

    @Test
    void should_return_exception_when_command_is_null() {

        assertThrows(CashPaymentValidationException.class,
                () -> CashPaymentValidator.validate(
                        createValidPayment(),
                        null
                ));
    }

    @Test
    void should_return_exception_when_payment_is_null() {

        assertThrows(CashPaymentValidationException.class,
                () -> CashPaymentValidator.validate(
                        null,
                        createValidCommand()
                ));
    }

    @Test
    void should_return_exception_when_payment_method_is_not_cash() {

        Payment payment = Payment.reconstitute(
                PAYMENT_ID,
                INVOICE_ID,
                AMOUNT,
                PaymentMethod.CARD,
                PaymentStatus.CREATED,
                FIXED_CLOCK.instant(),
                null,
                null
        );

        assertThrows(CashPaymentValidationException.class,
                () -> CashPaymentValidator.validate(
                        payment,
                        createValidCommand()
                ));
    }

    @Test
    void should_return_exception_when_currencies_do_not_match() {
        CashPaymentCompletionCommand command = new CashPaymentCompletionCommand(
                PAYMENT_ID,
                Money.of("376.98", USD));

        assertThrows(CashPaymentValidationException.class,
                () -> CashPaymentValidator.validate(
                        createValidPayment(),
                        command
                ));
    }

    @Test
    void should_return_exception_when_received_amount_is_less_than_payment_amount() {
        CashPaymentCompletionCommand command = new CashPaymentCompletionCommand(
                PAYMENT_ID,
                Money.of("21.98", EUR));

        assertThrows(CashPaymentValidationException.class,
                () -> CashPaymentValidator.validate(
                        createValidPayment(),
                        command
                ));
    }

    @Test
    void should_return_exception_when_payment_ids_do_not_match() {

        CashPaymentCompletionCommand command = new CashPaymentCompletionCommand(
                999L,
                createValidReceivedAmount()
        );

        assertThrows(
                CashPaymentValidationException.class,
                () -> CashPaymentValidator.validate(
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

    private CashPaymentCompletionCommand createValidCommand() {
        return new CashPaymentCompletionCommand(PAYMENT_ID, createValidReceivedAmount());
    }

    private Money createValidReceivedAmount() {
        return Money.of("350.00", EUR);
    }
}