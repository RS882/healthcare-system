package com.healthcare.billing.payment.processor;

import com.healthcare.billing.exception.CardPaymentServerUnavailableException;
import com.healthcare.billing.exception.CardPaymentVerificationResultException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CardPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.card_payment.CardPaymentGateway;
import com.healthcare.billing.payment.payment_method_details.card_payment.CardPaymentVerificationResult;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentVerificationStatus;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.payment.validation.CardPaymentValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Card payment processor tests:  ")
class CardPaymentProcessorTest {

    private static final Long INVOICE_ID = 12L;
    private static final Long PAYMENT_ID = 2L;

    private static final String PROVIDER_TRANSACTION_ID = "Test provider transaction ID  ";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Money AMOUNT = Money.of("328.29", EUR);

    private static final PaymentMethod METHOD = PaymentMethod.CARD;

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private Payment payment;

    private CardPaymentProcessor processor;

    @Mock
    private CardPaymentGateway paymentGateway;
    @Mock
    private CardPaymentValidator validator;

    @BeforeEach
    void setUp() {

        payment = Payment.reconstitute(
                PAYMENT_ID,
                INVOICE_ID,
                AMOUNT,
                METHOD,
                PaymentStatus.CREATED,
                FIXED_CLOCK.instant(),
                null,
                null
        );

        processor = new CardPaymentProcessor(
                paymentGateway,
                validator,
                new PaymentStateResolver(),
                FIXED_CLOCK);
    }

    @Test
    void should_complete_payment_when_card_processing_succeeds() {

        CardPaymentCompletionCommand command = createValidCommand();

        CardPaymentVerificationResult result =
                new CardPaymentVerificationResult(
                        PROVIDER_TRANSACTION_ID,
                        CardPaymentVerificationStatus.SUCCEEDED,
                        AMOUNT);

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID)).thenReturn(result);

        CardPaymentMethodDetails details = processor.process(payment, command);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), payment.getCompletedAt());

        assertNotNull(details);
        assertEquals(PROVIDER_TRANSACTION_ID, details.providerTransactionId());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_return_exception_when_card_payment_verification_result_is_null() {

        CardPaymentCompletionCommand command = createValidCommand();

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID)).thenReturn(null);

        assertThrows(CardPaymentVerificationResultException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_return_exception_when_amounts_do_not_match() {

        CardPaymentCompletionCommand command = createValidCommand();

        CardPaymentVerificationResult result =
                new CardPaymentVerificationResult(
                        PROVIDER_TRANSACTION_ID,
                        CardPaymentVerificationStatus.SUCCEEDED,
                        Money.of("15.97", EUR));

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID)).thenReturn(result);

        assertThrows(CardPaymentVerificationResultException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_return_exception_when_currencies_do_not_match() {

        CardPaymentCompletionCommand command = createValidCommand();

        CardPaymentVerificationResult result =
                new CardPaymentVerificationResult(
                        PROVIDER_TRANSACTION_ID,
                        CardPaymentVerificationStatus.SUCCEEDED,
                        Money.of(AMOUNT.amount(), USD));

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID)).thenReturn(result);

        assertThrows(CardPaymentVerificationResultException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_return_exception_when_provider_transaction_id_do_not_match() {

        CardPaymentCompletionCommand command = createValidCommand();

        CardPaymentVerificationResult result =
                new CardPaymentVerificationResult(
                        "WRONG_PROVIDER_TRANSACTION_ID",
                        CardPaymentVerificationStatus.SUCCEEDED,
                        AMOUNT);

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID)).thenReturn(result);

        assertThrows(CardPaymentVerificationResultException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @ParameterizedTest
    @EnumSource(value = CardPaymentVerificationStatus.class,
            names = {"PENDING", "FAILED"}
    )
    void should_return_exception_when_status_is_not_SUCCEEDED(CardPaymentVerificationStatus status) {

        CardPaymentCompletionCommand command = createValidCommand();

        CardPaymentVerificationResult result =
                new CardPaymentVerificationResult(
                        PROVIDER_TRANSACTION_ID,
                        status,
                        AMOUNT);

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID)).thenReturn(result);

        assertThrows(CardPaymentVerificationResultException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_not_start_processing_when_validation_fails() {

        CardPaymentCompletionCommand command =
                new CardPaymentCompletionCommand(9999L, PROVIDER_TRANSACTION_ID);

        doThrow(PaymentValidationException.class).when(validator)
                .validate(any(), any(CardPaymentCompletionCommand.class));

        assertThrows(PaymentValidationException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verifyNoInteractions(paymentGateway);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_return_exception_when_gateway_is_unavailable() {

        CardPaymentCompletionCommand command =
                new CardPaymentCompletionCommand(PAYMENT_ID, PROVIDER_TRANSACTION_ID);

        when(paymentGateway.verifyTransaction(PROVIDER_TRANSACTION_ID))
                .thenThrow(CardPaymentServerUnavailableException.class);

        assertThrows(CardPaymentServerUnavailableException.class,
                () -> processor.process(payment, command));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(paymentGateway).verifyTransaction(PROVIDER_TRANSACTION_ID);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_return_supported_command_type() {

        Class<?> result = processor.supportedCommandType();

        assertEquals(CardPaymentCompletionCommand.class, result);
    }

    private CardPaymentCompletionCommand createValidCommand() {
        return new CardPaymentCompletionCommand(PAYMENT_ID, PROVIDER_TRANSACTION_ID);
    }
}