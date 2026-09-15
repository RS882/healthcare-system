package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.PaymentStateMethodException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment tests: ")
class PaymentTest {

    private Payment payment;
    private final PaymentStateResolver resolver = new PaymentStateResolver();

    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @BeforeEach
    void setUp() {

        payment = new Payment(
                INVOICE_ID,
                AMOUNT,
                METHOD,
                FIXED_CLOCK.instant()
        );
    }

    @Test
    void should_transition_status_from_created_to_pending_when_started() {

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(26L);

        payment.start(eventTime, resolver);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(eventTime, payment.getUpdatedAt());
        assertNull(payment.getCompletedAt());
    }

    @Test
    void should_transition_status_from_created_to_cancelled_when_cancelled() {

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(26L);

        payment.cancel(eventTime, resolver);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        assertEquals(eventTime, payment.getUpdatedAt());
        assertNull(payment.getCompletedAt());
    }

    @Test
    void should_transition_status_from_pending_to_completed_when_completed() {

        Instant startTime = FIXED_CLOCK.instant().plusSeconds(26);
        Instant completeTime = FIXED_CLOCK.instant().plusSeconds(32);

        payment.start(startTime, resolver);
        payment.complete(completeTime, resolver);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(completeTime, payment.getUpdatedAt());
        assertEquals(completeTime, payment.getCompletedAt());
    }

    @Test
    void should_transition_status_from_pending_to_cancelled_when_cancelled() {

        Instant startTime = FIXED_CLOCK.instant().plusSeconds(26);
        Instant cancelTime = FIXED_CLOCK.instant().plusSeconds(32);

        payment.start(startTime, resolver);
        payment.cancel(cancelTime, resolver);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        assertEquals(cancelTime, payment.getUpdatedAt());
        assertNull(payment.getCompletedAt());
    }

    @Test
    void should_throw_exception_when_start_is_called_from_pending_state() {
        payment.start(FIXED_CLOCK.instant().plusSeconds(26), resolver);

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(32);

        assertThrows(PaymentStateMethodException.class,
                () -> payment.start(eventTime, resolver)
        );
    }

    @Test
    void should_throw_exception_when_status_from_created_to_completed_when_completed() {

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(26L);

        assertThrows(PaymentStateMethodException.class,
                () -> payment.complete(eventTime, resolver));
    }

    @Test
    void should_throw_exception_when_resolver_is_null() {
        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(26);

        assertThrows(
                PaymentValidationException.class,
                () -> payment.start(eventTime, null)
        );
    }

    @ParameterizedTest(name = "Test {index}: status [{arguments}]")
    @EnumSource(
            value = PaymentStatus.class,
            names = {"COMPLETED", "CANCELLED"}
    )
    void should_throw_exception_when_start_is_called_from_terminal_state(PaymentStatus status) {

        move_to_status(status);

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(40L);

        assertThrows(PaymentStateMethodException.class,
                () -> payment.start(eventTime, resolver));
    }

    @ParameterizedTest(name = "Test {index}: status [{arguments}]")
    @EnumSource(
            value = PaymentStatus.class,
            names = {"COMPLETED", "CANCELLED"}
    )
    void should_throw_exception_when_complete_is_called_from_terminal_state(PaymentStatus status) {

        move_to_status(status);

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(40L);

        assertThrows(PaymentStateMethodException.class,
                () -> payment.complete(eventTime, resolver));
    }

    @ParameterizedTest(name = "Test {index}: status [{arguments}]")
    @EnumSource(
            value = PaymentStatus.class,
            names = {"COMPLETED", "CANCELLED"}
    )
    void should_throw_exception_when_cancel_is_called_from_terminal_state(PaymentStatus status) {

        move_to_status(status);

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(40L);

        assertThrows(PaymentStateMethodException.class,
                () -> payment.cancel(eventTime, resolver));
    }

    @Test
    void should_create_payment_in_created_state() {
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), payment.getCreatedAt());
        assertNull(payment.getUpdatedAt());
        assertNull(payment.getCompletedAt());
        assertNull(payment.getId());
    }

    @ParameterizedTest(name = "Test {index}:  [{arguments}]")
    @MethodSource("incorrectPayment")
    void should_throw_exception_when_payment_is_created_with_incorrect_data(PaymentData data) {

        assertThrows(PaymentValidationException.class,
                () -> new Payment(
                        data.invoiceId,
                        data.amount,
                        data.method,
                        data.createdAt));
    }

    private static Stream<Arguments> incorrectPayment() {
        return Stream.of(
                Arguments.of(
                        new PaymentData(
                                INVOICE_ID,
                                AMOUNT,
                                METHOD,
                                null)),

                Arguments.of(new PaymentData(
                        null,
                        AMOUNT,
                        METHOD,
                        FIXED_CLOCK.instant())),

                Arguments.of(new PaymentData(
                        -287L,
                        AMOUNT,
                        METHOD,
                        FIXED_CLOCK.instant())),

                Arguments.of(new PaymentData(
                        0L,
                        AMOUNT,
                        METHOD,
                        FIXED_CLOCK.instant())),

                Arguments.of(new PaymentData(
                        INVOICE_ID,
                        null,
                        METHOD,
                        FIXED_CLOCK.instant())),

                Arguments.of(new PaymentData(
                        INVOICE_ID,
                        Money.zero(CURRENCY),
                        METHOD,
                        FIXED_CLOCK.instant())),


                Arguments.of(new PaymentData(
                        INVOICE_ID,
                        Money.of("-29837.87", CURRENCY),
                        METHOD,
                        FIXED_CLOCK.instant())),

                Arguments.of(new PaymentData(
                        INVOICE_ID,
                        AMOUNT,
                        null,
                        FIXED_CLOCK.instant()))
        );
    }

    @Test
    void should_throw_exception_when_event_time_is_null() {

        assertThrows(PaymentValidationException.class,
                () -> payment.start(null, resolver));

    }

    @Test
    void should_throw_exception_when_event_time_is_less_than_created_at() {

        Instant eventTime = FIXED_CLOCK.instant().minusSeconds(15);

        assertThrows(PaymentValidationException.class,
                () -> payment.start(eventTime, resolver)
        );
    }

    @Test
    void should_throw_exception_when_event_time_is_before_updated_at() {
        payment.start(FIXED_CLOCK.instant().plusSeconds(26), resolver);

        Instant eventTime = FIXED_CLOCK.instant().plusSeconds(20);

        assertThrows(
                PaymentValidationException.class,
                () -> payment.complete(eventTime, resolver)
        );
    }

    private void move_to_status(PaymentStatus status) {
        Instant firstEventTime = FIXED_CLOCK.instant().plusSeconds(26);
        Instant secondEventTime = FIXED_CLOCK.instant().plusSeconds(32);

        switch (status) {
            case COMPLETED -> {
                payment.start(firstEventTime, resolver);
                payment.complete(secondEventTime, resolver);
            }
            case CANCELLED -> payment.cancel(firstEventTime, resolver);
            default -> throw new IllegalArgumentException("Unsupported terminal status: " + status);
        }
    }

    private record PaymentData(Long invoiceId,
                               Money amount,
                               PaymentMethod method,
                               Instant createdAt) {

    }
}