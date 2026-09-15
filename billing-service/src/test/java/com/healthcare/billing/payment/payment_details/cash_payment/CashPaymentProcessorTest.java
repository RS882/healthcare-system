package com.healthcare.billing.payment.payment_details.cash_payment;

import com.healthcare.billing.exception.CashChangeCalculatorException;
import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_details.CashPaymentDetails;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.CashChangeCalculator;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.ChangeBreakdown;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.payment.validation.CashPaymentValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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

        payment = new Payment(
                INVOICE_ID,
                AMOUNT,
                METHOD,
                FIXED_CLOCK.instant()
        );

        processor = new CashPaymentProcessor(
                validator,
                calculator,
                new PaymentStateResolver(),
                FIXED_CLOCK);
    }

    @Test
    void should_complete_payment_when_cash_processing_succeeds() {

        Money receivedAmount = getDefaultReceivedAmount();

        ChangeBreakdown breakdown = getDefaultBreakdown();

        when(calculator.calculate(AMOUNT, receivedAmount)).thenReturn(breakdown);

        CashPaymentDetails details = processor.process(payment, receivedAmount);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(details);
        assertSame(payment, details.getPayment());
        assertEquals(receivedAmount, details.getReceivedAmount());
        assertEquals(breakdown, details.getChangeBreakdown());

        InOrder inOrder = inOrder(validator, calculator);

        inOrder.verify(validator).validate(payment, receivedAmount);
        inOrder.verify(calculator).calculate(AMOUNT, receivedAmount);
    }

    @Test
    void should_keep_payment_created_when_validation_fails() {

        Money receivedAmount = getDefaultReceivedAmount();

        doThrow(new CashPaymentValidationException("Error"))
                .when(validator).validate(payment, receivedAmount);

        assertThrows(CashPaymentValidationException.class,
                () -> processor.process(payment, receivedAmount));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());

        verify(validator).validate(payment, receivedAmount);

        verify(calculator, never()).calculate(AMOUNT, receivedAmount);
    }

    @Test
    void should_cancel_payment_when_change_calculation_fails() {

        Money receivedAmount = getDefaultReceivedAmount();

        CashChangeCalculatorException expected =
                new CashChangeCalculatorException("Error");

        doThrow(expected)
                .when(calculator)
                .calculate(AMOUNT, receivedAmount);

        CashChangeCalculatorException actual =
                assertThrows(
                        CashChangeCalculatorException.class,
                        () -> processor.process(payment, receivedAmount)
                );

        assertSame(expected, actual);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());

        InOrder inOrder = inOrder(validator, calculator);

        inOrder.verify(validator).validate(payment, receivedAmount);
        inOrder.verify(calculator).calculate(AMOUNT, receivedAmount);
    }

    @Test
    void should_cancel_payment_when_currency_is_unsupported() {

        Money receivedAmount = getDefaultReceivedAmount();

        UnsupportedCurrencyException expected = new UnsupportedCurrencyException("Error");

        doThrow(expected)
                .when(calculator).calculate(AMOUNT, receivedAmount);

        UnsupportedCurrencyException actual = assertThrows(UnsupportedCurrencyException.class,
                () -> processor.process(payment, receivedAmount));

        assertSame(expected, actual);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());

        InOrder inOrder = inOrder(validator, calculator);

        inOrder.verify(validator).validate(payment, receivedAmount);
        inOrder.verify(calculator).calculate(AMOUNT, receivedAmount);
    }

    @Test
    void should_complete_payment_when_received_amount_equals_payment_amount() {

        Money receivedAmount = AMOUNT;

        Money changedAmount = Money.zero(CURRENCY);
        Map<String, Long> denominations = new LinkedHashMap<>();

        ChangeBreakdown breakdown = new ChangeBreakdown(changedAmount, denominations);

        when(calculator.calculate(AMOUNT, receivedAmount)).thenReturn(breakdown);

        CashPaymentDetails details = processor.process(payment, receivedAmount);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(details);
        assertNotNull(details.getChangeBreakdown());
        assertSame(payment, details.getPayment());
        assertEquals(receivedAmount, details.getReceivedAmount());
        assertEquals(breakdown, details.getChangeBreakdown());
        assertTrue(details.getChangeBreakdown().change().isZero());
        assertTrue(details.getChangeBreakdown().denominations().isEmpty());

        InOrder inOrder = inOrder(validator, calculator);

        inOrder.verify(validator).validate(payment, receivedAmount);
        inOrder.verify(calculator).calculate(AMOUNT, receivedAmount);
    }

    private Money getDefaultReceivedAmount() {
        return Money.of("350.00", CURRENCY);
    }

    private ChangeBreakdown getDefaultBreakdown() {

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