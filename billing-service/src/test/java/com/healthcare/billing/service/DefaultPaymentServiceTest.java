package com.healthcare.billing.service;

import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.exception.PaymentNotFoundException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.persistence.InvoiceStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default payment service tests: ")
@ExtendWith(MockitoExtension.class)
class DefaultPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceStore invoiceStore;

    @Mock
    private Clock clock;

    @InjectMocks
    private DefaultPaymentService paymentService;

    private static final Long ID = 73L;
    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final PaymentStatus STATUS = PaymentStatus.PENDING;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @Test
    void should_find_payment_by_id() {

        Payment payment = getReconstitutedPayment();

        when(paymentRepository.findById(ID)).thenReturn(Optional.of(payment));

        Payment foundPayment = paymentService.retrievePaymentById(ID);

        assertNotNull(foundPayment);
        assertEquals(payment, foundPayment);

        verify(paymentRepository).findById(ID);
    }

    @Test
    void should_get_exception_when_payment_not_found() {

        when(paymentRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.retrievePaymentById(ID));

        verify(paymentRepository).findById(ID);
    }

    @Test
    void should_get_exception_when_payment_id_is_null() {

        assertThrows(PaymentValidationException.class,
                () -> paymentService.retrievePaymentById(null));

        verifyNoInteractions(paymentRepository);
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @ValueSource(longs = {
            0L,
            -727L
    })
    void should_get_exception_when_payment_id_is_incorrect(Long id) {

        assertThrows(PaymentValidationException.class,
                () -> paymentService.retrievePaymentById(id));

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void should_return_payments_by_invoice_id() {

        Payment payment1 = getReconstitutedPayment(176L);
        Payment payment2 = getReconstitutedPayment(45L);

        List<Payment> payments = List.of(payment1, payment2);

        when(paymentRepository.findByInvoiceId(INVOICE_ID)).thenReturn(payments);

        List<Payment> foundPayments = paymentService.retrievePaymentsByInvoiceId(INVOICE_ID);

        assertNotNull(foundPayments);
        assertEquals(payments, foundPayments);

        verify(paymentRepository).findByInvoiceId(INVOICE_ID);
    }

    @Test
    void should_return_empty_list_when_no_payments_by_invoice_id() {

        when(paymentRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of());

        List<Payment> foundPayments = paymentService.retrievePaymentsByInvoiceId(INVOICE_ID);

        assertNotNull(foundPayments);

        assertTrue(foundPayments.isEmpty());

        verify(paymentRepository).findByInvoiceId(INVOICE_ID);
    }

    @Test
    void should_return_exception_when_invoice_id_is_null() {

        assertThrows(PaymentValidationException.class,
                () -> paymentService.retrievePaymentsByInvoiceId(null));

        verifyNoInteractions(paymentRepository);
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @ValueSource(longs = {
            0L,
            -53L
    })
    void should_get_exception_when_invoice_id_is_incorrect(Long id) {

        assertThrows(PaymentValidationException.class,
                () -> paymentService.retrievePaymentsByInvoiceId(id));

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void should_return_created_payment() {
        Instant fixedInstant = FIXED_CLOCK.instant();

        Payment createdPayment = getCreatedPayment();

        when(invoiceStore.existsById(INVOICE_ID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(createdPayment);
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());

        Payment savedPayment = paymentService.createPayment(INVOICE_ID, AMOUNT, METHOD);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        assertNotNull(savedPayment);
        assertSame(createdPayment, savedPayment);
        verify(paymentRepository).save(paymentCaptor.capture());
        verify(invoiceStore).existsById(INVOICE_ID);

        Payment capturedPayment = paymentCaptor.getValue();

        assertNull(capturedPayment.getId());
        assertEquals(INVOICE_ID, capturedPayment.getInvoiceId());
        assertEquals(0, AMOUNT.compareTo(capturedPayment.getAmount()));
        assertEquals(METHOD, capturedPayment.getMethod());
        assertEquals(fixedInstant, capturedPayment.getCreatedAt());
        assertEquals(PaymentStatus.CREATED, capturedPayment.getStatus());
        assertNull(capturedPayment.getUpdatedAt());
        assertNull(capturedPayment.getCompletedAt());
    }

    @Test
    void should_return_exception_when_invoice_id_is_null_upon_creation() {

        assertThrows(PaymentValidationException.class,
                () -> paymentService.createPayment(null, AMOUNT, METHOD));

        verifyNoInteractions(paymentRepository, invoiceStore, clock);
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @ValueSource(longs = {
            0L,
            -63L
    })
    void should_get_exception_when_invoice_id_is_incorrect_upon_creation(Long id) {

        assertThrows(PaymentValidationException.class,
                () -> paymentService.createPayment(id, AMOUNT, METHOD));

        verifyNoInteractions(paymentRepository, invoiceStore, clock);
    }

    @Test
    void should_return_exception_when_invoice_not_found() {

        when(invoiceStore.existsById(INVOICE_ID)).thenReturn(false);

        assertThrows(InvoiceNotFoundException.class,
                () -> paymentService.createPayment(INVOICE_ID, AMOUNT, METHOD));

        verifyNoInteractions(paymentRepository, clock);
        verify(invoiceStore).existsById(INVOICE_ID);
    }

    private Payment getReconstitutedPayment() {
        return getReconstitutedPayment(ID);
    }

    private Payment getReconstitutedPayment(Long id) {
        return Payment.reconstitute(
                id,
                INVOICE_ID,
                AMOUNT,
                METHOD,
                STATUS,
                FIXED_CLOCK.instant(),
                FIXED_CLOCK.instant().plusSeconds(30),
                null
        );
    }

    private Payment getCreatedPayment() {
        return Payment.reconstitute(
                ID,
                INVOICE_ID,
                AMOUNT,
                METHOD,
                PaymentStatus.CREATED,
                FIXED_CLOCK.instant(),
                null,
                null
        );
    }
}