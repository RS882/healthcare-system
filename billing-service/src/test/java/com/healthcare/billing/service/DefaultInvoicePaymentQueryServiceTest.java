package com.healthcare.billing.service;

import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.exception.PaymentJpaAdapterException;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.InvoicePaymentSummary;
import com.healthcare.billing.payment.model.PaymentBalance;
import com.healthcare.billing.payment.model.enums.PaymentCoverageStatus;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.persistence.InvoiceStore;
import com.healthcare.billing.service.interfaces.PaymentBalanceCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default invoice payment query service tests: ")
@ExtendWith(MockitoExtension.class)
class DefaultInvoicePaymentQueryServiceTest {

    @Mock
    private InvoiceStore invoiceStore;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentBalanceCalculator paymentBalanceCalculator;

    @InjectMocks
    private DefaultInvoicePaymentQueryService defaultInvoicePaymentQueryService;

    private static final Currency CURRENCY_EUR = Currency.getInstance("EUR");
    private static final Currency CURRENCY_USD = Currency.getInstance("USD");
    private static final Long INVOICE_ID = 3L;

    @ParameterizedTest(name = "Test {index}:  [{arguments}]")
    @MethodSource("getPaymentTestData")
    void should_return_correct_invoice_payment_summary(TestDataPaymentSummary testData) {

        Money paidAmount = resolveAmount(testData.paidAmount());
        Money invoiceAmount = resolveAmount(testData.invoiceAmount());

        PaymentBalance paymentBalance = resolvePaymentBalance(
                testData.remainingAmount(),
                testData.overpaidAmount()
        );

        Invoice invoice = mock(Invoice.class);
        when(invoice.getTotalAmount()).thenReturn(invoiceAmount);
        when(invoice.getId()).thenReturn(INVOICE_ID);
        when(invoice.getStatus()).thenReturn(testData.invoiceStatus());

        when(invoiceStore.findById(INVOICE_ID)).thenReturn(invoice);
        when(paymentRepository.calculatePaidAmount(INVOICE_ID)).thenReturn(paidAmount);
        when(paymentBalanceCalculator.calculateInvoicePaymentBalance(invoiceAmount, paidAmount))
                .thenReturn(paymentBalance);

        InvoicePaymentSummary result = defaultInvoicePaymentQueryService.getPaymentSummary(INVOICE_ID);

        assertNotNull(result);
        assertEquals(INVOICE_ID, result.invoiceId());
        assertEquals(paidAmount, result.paidAmount());
        assertEquals(testData.invoiceStatus(), result.invoiceStatus());
        assertEquals(testData.paymentCoverageStatus(), result.paymentCoverageStatus());
        assertEquals(invoiceAmount, result.totalAmount());
        assertNotNull(result.balance());
        assertSame(paymentBalance, result.balance());

        verify(invoiceStore).findById(INVOICE_ID);
        verify(paymentRepository).calculatePaidAmount(INVOICE_ID);
        verify(paymentBalanceCalculator).calculateInvoicePaymentBalance(invoiceAmount, paidAmount);
    }

    @ParameterizedTest(name = "Test {index}: invoice id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0,
            -172
    })
    void should_return_exception_when_invoice_id_is_negative_or_zero_or_null(Long id) {

        assertThrows(InvoiceValidationException.class,
                () -> defaultInvoicePaymentQueryService.getPaymentSummary(id));

        verifyNoInteractions(invoiceStore, paymentRepository, paymentBalanceCalculator);

    }

    @Test
    void should_return_exception_when_invoice_not_found() {

        when(invoiceStore.findById(INVOICE_ID))
                .thenThrow(new InvoiceNotFoundException(INVOICE_ID));

        assertThrows(InvoiceNotFoundException.class,
                () -> defaultInvoicePaymentQueryService.getPaymentSummary(INVOICE_ID));

        verify(invoiceStore).findById(INVOICE_ID);
        verifyNoInteractions(paymentRepository, paymentBalanceCalculator);
    }

    @Test
    void should_return_exception_when_calculate_paid_amount_trow_exception() {

        Invoice invoice = mock(Invoice.class);

        when(invoiceStore.findById(INVOICE_ID)).thenReturn(invoice);
        when(paymentRepository.calculatePaidAmount(INVOICE_ID))
                .thenThrow(new PaymentJpaAdapterException("Failed to calculate paid amount"));

        assertThrows(PaymentJpaAdapterException.class,
                () -> defaultInvoicePaymentQueryService.getPaymentSummary(INVOICE_ID));

        verify(invoiceStore).findById(INVOICE_ID);
        verify(paymentRepository).calculatePaidAmount(INVOICE_ID);
        verifyNoInteractions(paymentBalanceCalculator);
    }

    @Test
    void should_return_exception_when_currencies_do_not_match() {

        Money paidAmount = resolveAmount("230.78");
        Money invoiceAmount = Money.of("230.78", CURRENCY_USD);

        Invoice invoice = mock(Invoice.class);
        when(invoice.getTotalAmount()).thenReturn(invoiceAmount);

        when(invoiceStore.findById(INVOICE_ID)).thenReturn(invoice);
        when(paymentRepository.calculatePaidAmount(INVOICE_ID)).thenReturn(paidAmount);
        when(paymentBalanceCalculator.calculateInvoicePaymentBalance(invoiceAmount, paidAmount))
                .thenThrow(new CurrencyMismatchException(CURRENCY_EUR, CURRENCY_USD));

        assertThrows(CurrencyMismatchException.class,
                () -> defaultInvoicePaymentQueryService.getPaymentSummary(INVOICE_ID));

        verify(invoiceStore).findById(INVOICE_ID);
        verify(paymentRepository).calculatePaidAmount(INVOICE_ID);
        verify(paymentBalanceCalculator).calculateInvoicePaymentBalance(invoiceAmount, paidAmount);
    }

    private static Stream<Arguments> getPaymentTestData() {
        return Stream.of(
                Arguments.of(
                        new TestDataPaymentSummary(
                                "230.78",
                                "450.2",
                                "219.42",
                                "",
                                InvoiceStatus.ISSUED,
                                PaymentCoverageStatus.PARTIALLY_PAID)),
                Arguments.of(
                        new TestDataPaymentSummary(
                                "230.78",
                                "230.78",
                                "",
                                "",
                                InvoiceStatus.PAID,
                                PaymentCoverageStatus.FULLY_PAID)),
                Arguments.of(
                        new TestDataPaymentSummary(
                                "450.2",
                                "230.78",
                                "",
                                "219.42",
                                InvoiceStatus.PAID,
                                PaymentCoverageStatus.OVERPAID)),
                Arguments.of(
                        new TestDataPaymentSummary(
                                "",
                                "230.78",
                                "230.78",
                                "",
                                InvoiceStatus.ISSUED,
                                PaymentCoverageStatus.UNPAID)),
                Arguments.of(
                        new TestDataPaymentSummary(
                                "230.78",
                                "230.78",
                                "",
                                "",
                                InvoiceStatus.ISSUED,
                                PaymentCoverageStatus.FULLY_PAID))
        );
    }

    private PaymentBalance resolvePaymentBalance(String remainingAmount,
                                                 String overpaidAmount) {

        return PaymentBalance.builder()
                .remainingAmount(resolveAmount(remainingAmount))
                .overpaidAmount(resolveAmount(overpaidAmount))
                .build();
    }

    private Money resolveAmount(String amount) {

        return amount.isBlank() ?
                Money.zero(CURRENCY_EUR) :
                Money.of(amount, CURRENCY_EUR);
    }

    private record TestDataPaymentSummary(
            String paidAmount,
            String invoiceAmount,
            String remainingAmount,
            String overpaidAmount,
            InvoiceStatus invoiceStatus,
            PaymentCoverageStatus paymentCoverageStatus) {
    }
}
