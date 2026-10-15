package com.healthcare.billing.payment.processor;

import com.healthcare.billing.exception.CashChangeCalculatorException;
import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CashPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.cash_payment.CashChangeCalculator;
import com.healthcare.billing.payment.payment_method_details.cash_payment.ChangeBreakdown;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.payment.validation.CashPaymentValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Cash payment processor tests:  ")
class CashPaymentProcessorTest {

    private static final Long INVOICE_ID = 12L;
    private static final Long PAYMENT_ID = 2L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);

    private static final PaymentMethod METHOD = PaymentMethod.CASH;

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private Payment payment;

    private CashPaymentProcessor processor;

    @Mock
    private CashPaymentValidator validator;

    @Mock
    private CashChangeCalculator calculator;

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

        processor = new CashPaymentProcessor(
                calculator,
                new PaymentStateResolver(),
                FIXED_CLOCK,
                validator);

    }

    @Test
    void should_complete_payment_when_cash_processing_succeeds() {

        Money receivedAmount = createValidReceivedAmount();

        ChangeBreakdown breakdown = createValidBreakdown();

        CashPaymentCompletionCommand command = createValidCommand();

        when(calculator.calculate(AMOUNT, receivedAmount)).thenReturn(breakdown);

        CashPaymentMethodDetails details = processor.process(payment, command);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), payment.getCompletedAt());

        assertNotNull(details);
        assertEquals(receivedAmount, details.receivedAmount());
        assertEquals(breakdown, details.changeBreakdown());

        verify(calculator).calculate(AMOUNT, receivedAmount);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_cancel_payment_when_change_calculation_fails() {

        CashPaymentCompletionCommand command = createValidCommand();

        Money receivedAmount = command.receivedAmount();

        CashChangeCalculatorException expected =
                new CashChangeCalculatorException("Error");

        doThrow(expected)
                .when(calculator)
                .calculate(AMOUNT, receivedAmount);

        CashChangeCalculatorException actual =
                assertThrows(
                        CashChangeCalculatorException.class,
                        () -> processor.process(payment, command)
                );

        assertSame(expected, actual);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());

        verify(calculator).calculate(AMOUNT, receivedAmount);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_cancel_payment_when_currency_is_unsupported() {

        CashPaymentCompletionCommand command = createValidCommand();

        Money receivedAmount = command.receivedAmount();

        UnsupportedCurrencyException expected = new UnsupportedCurrencyException("Error");

        doThrow(expected)
                .when(calculator).calculate(AMOUNT, receivedAmount);

        UnsupportedCurrencyException actual = assertThrows(UnsupportedCurrencyException.class,
                () -> processor.process(payment, command));

        assertSame(expected, actual);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());

        verify(calculator).calculate(AMOUNT, receivedAmount);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_complete_payment_when_received_amount_equals_payment_amount() {

        Money receivedAmount = AMOUNT;

        CashPaymentCompletionCommand command = new CashPaymentCompletionCommand(PAYMENT_ID, receivedAmount);

        Money changedAmount = Money.zero(CURRENCY);
        Map<String, Long> denominations = new LinkedHashMap<>();

        ChangeBreakdown breakdown = new ChangeBreakdown(changedAmount, denominations);

        when(calculator.calculate(AMOUNT, receivedAmount)).thenReturn(breakdown);

        CashPaymentMethodDetails details = processor.process(payment, command);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(details);
        assertNotNull(details.changeBreakdown());
        assertEquals(receivedAmount, details.receivedAmount());
        assertEquals(breakdown, details.changeBreakdown());
        assertTrue(details.changeBreakdown().change().isZero());
        assertTrue(details.changeBreakdown().denominations().isEmpty());

        verify(calculator).calculate(AMOUNT, receivedAmount);
        verify(validator).validate(payment, command);
    }

    @Test
    void should_not_start_processing_when_validation_fails() {

        CashPaymentCompletionCommand command =
                new CashPaymentCompletionCommand(999L, createValidReceivedAmount());

        doThrow(PaymentValidationException.class).when(validator)
                .validate(any(), any(CashPaymentCompletionCommand.class));

        assertThrows(
                PaymentValidationException.class,
                () -> processor.process(payment, command)
        );

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verifyNoInteractions(calculator);
        verify(validator).validate(payment, command);
    }

    private CashPaymentCompletionCommand createValidCommand() {
        return new CashPaymentCompletionCommand(PAYMENT_ID, createValidReceivedAmount());
    }

    private Money createValidReceivedAmount() {
        return Money.of("350.00", CURRENCY);
    }

    private ChangeBreakdown createValidBreakdown() {

        Money changedAmount = Money.of("21.71", CURRENCY);
        Map<String, Long> denominations = new LinkedHashMap<>();

        denominations.put("20 EUR", 1L);
        denominations.put("1 EUR", 1L);
        denominations.put("50 cent", 1L);
        denominations.put("20 cent", 1L);
        denominations.put("1 cent", 1L);

        return new ChangeBreakdown(changedAmount, denominations);
    }
}