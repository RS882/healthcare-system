package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.ChangeBreakdown;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PaymentCompletionResultTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final PaymentStatus PAYMENT_COMPLETED = PaymentStatus.COMPLETED;
    private static final Instant COMPLETED_AT =
            Instant.parse("2026-09-04T10:00:00Z");

    @ParameterizedTest(name = "Test {index}: invoice status [{arguments}]")
    @EnumSource(
            value = InvoiceStatus.class,
            names = {"ISSUED", "PAID"}
    )
    void should_return_payment_completion_result(InvoiceStatus invoiceStatus) {

        Long paymentId = 34L;
        Long invoiceId = 123L;
        CashPaymentMethodDetails details = getCashPaymentMethodDetails();

        PaymentCompletionResult<CashPaymentMethodDetails> result = new PaymentCompletionResult<>(
                paymentId,
                invoiceId,
                PAYMENT_COMPLETED,
                invoiceStatus,
                COMPLETED_AT,
                details
        );

        assertNotNull(result);
        assertEquals(paymentId, result.paymentId());
        assertEquals(invoiceId, result.invoiceId());
        assertEquals(PAYMENT_COMPLETED, result.paymentStatus());
        assertEquals(invoiceStatus, result.invoiceStatus());
        assertEquals(COMPLETED_AT, result.completedAt());
        assertSame(details, result.methodDetails());
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void should_return_exception_when_payment_id_is_incorrect(Long paymentId) {

        Long invoiceId = 123L;
        CashPaymentMethodDetails details = getCashPaymentMethodDetails();
        InvoiceStatus invoiceStatus = InvoiceStatus.PAID;

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentCompletionResult<>(
                        paymentId,
                        invoiceId,
                        PAYMENT_COMPLETED,
                        invoiceStatus,
                        COMPLETED_AT,
                        details
                ));
    }

    @ParameterizedTest(name = "Test {index}: invoice id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void should_return_exception_when_invoice_id_is_incorrect(Long invoiceId) {

        Long paymentId = 123L;
        CashPaymentMethodDetails details = getCashPaymentMethodDetails();
        InvoiceStatus invoiceStatus = InvoiceStatus.PAID;

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentCompletionResult<>(
                        paymentId,
                        invoiceId,
                        PAYMENT_COMPLETED,
                        invoiceStatus,
                        COMPLETED_AT,
                        details
                ));
    }

    @ParameterizedTest(name = "Test {index}: payment status [{arguments}]")
    @NullSource
    @EnumSource(
            value = PaymentStatus.class,
            names = {"CREATED", "PENDING", "CANCELLED"}
    )
    void should_return_exception_when_payment_status_is_incorrect(PaymentStatus paymentStatus) {

        Long paymentId = 123L;
        Long invoiceId = 56L;
        CashPaymentMethodDetails details = getCashPaymentMethodDetails();
        InvoiceStatus invoiceStatus = InvoiceStatus.PAID;

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentCompletionResult<>(
                        paymentId,
                        invoiceId,
                        paymentStatus,
                        invoiceStatus,
                        COMPLETED_AT,
                        details
                ));
    }

    @ParameterizedTest(name = "Test {index}: invoice status [{arguments}]")
    @NullSource
    @EnumSource(
            value = InvoiceStatus.class,
            names = {"DRAFT", "CANCELLED"}
    )
    void should_return_exception_when_invoice_status_is_incorrect(InvoiceStatus invoiceStatus) {

        Long paymentId = 123L;
        Long invoiceId = 56L;
        CashPaymentMethodDetails details = getCashPaymentMethodDetails();

        assertThrows(InvoiceValidationException.class,
                () -> new PaymentCompletionResult<>(
                        paymentId,
                        invoiceId,
                        PAYMENT_COMPLETED,
                        invoiceStatus,
                        COMPLETED_AT,
                        details
                ));
    }

    @Test
    void should_return_exception_when_completed_at_is_null() {

        Long paymentId = 123L;
        Long invoiceId = 56L;
        CashPaymentMethodDetails details = getCashPaymentMethodDetails();
        InvoiceStatus invoiceStatus = InvoiceStatus.PAID;

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentCompletionResult<>(
                        paymentId,
                        invoiceId,
                        PAYMENT_COMPLETED,
                        invoiceStatus,
                        null,
                        details
                ));
    }

    @Test
    void should_return_exception_when_method_details_is_null() {

        Long paymentId = 123L;
        Long invoiceId = 56L;
        InvoiceStatus invoiceStatus = InvoiceStatus.PAID;

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentCompletionResult<>(
                        paymentId,
                        invoiceId,
                        PAYMENT_COMPLETED,
                        invoiceStatus,
                        COMPLETED_AT,
                        null
                ));
    }

    private CashPaymentMethodDetails getCashPaymentMethodDetails() {

        Money receivedAmount = Money.of("100.00", EUR);

        Money change = Money.of("25.82", EUR);

        Map<String, Long> denominations = new LinkedHashMap<>();

        denominations.put("20 EUR", 1L);
        denominations.put("5 EUR", 1L);
        denominations.put("50 cent", 1L);
        denominations.put("20 cent", 1L);
        denominations.put("10 cent", 1L);
        denominations.put("2 cent", 1L);

        ChangeBreakdown changeBreakdown = new ChangeBreakdown(change, denominations);

        return new CashPaymentMethodDetails(
                receivedAmount,
                changeBreakdown
        );
    }
}