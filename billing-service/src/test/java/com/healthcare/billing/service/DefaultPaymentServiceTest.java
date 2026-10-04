package com.healthcare.billing.service;

import com.healthcare.billing.exception.PaymentNotFoundException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.repository.PaymentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default payment service tests: ")
class DefaultPaymentServiceTest {

    private PaymentRepository paymentRepository;

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

    @BeforeEach
    void setUp() {

        paymentRepository = mock(PaymentRepository.class);

        paymentService = new DefaultPaymentService(paymentRepository);
    }

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
}