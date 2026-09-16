package com.healthcare.billing.payment.persistence.mapper;

import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.exception.PaymentMapperException;
import com.healthcare.billing.exception.PaymentReconstitutionException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment persistence mapper tests: ")
class PaymentPersistenceMapperTest {

    private static final Long ID = 73L;
    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private PaymentPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentPersistenceMapper();
    }

    @Test
    void should_map_new_payment_to_entity() {

        Payment payment = getCreatedPayment();

        PaymentEntity paymentEntity = mapper.toEntity(payment);

        assertNotNull(paymentEntity);
        assertNull(paymentEntity.getId());
        assertEquals(INVOICE_ID, paymentEntity.getInvoiceId());
        assertEquals(0, AMOUNT.amount().compareTo(paymentEntity.getAmount()));
        assertEquals(AMOUNT.currency().getCurrencyCode(), paymentEntity.getCurrency());
        assertEquals(METHOD, paymentEntity.getMethod());
        assertEquals(PaymentStatus.CREATED, paymentEntity.getStatus());
        assertEquals(FIXED_CLOCK.instant(), paymentEntity.getCreatedAt());
        assertNull(paymentEntity.getUpdatedAt());
        assertNull(paymentEntity.getCompletedAt());
    }

    @Test
    void should_map_existing_payment_to_entity() {

        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(7);
        PaymentStatus status = PaymentStatus.PENDING;
        Payment payment = getReconstitutedPayment(status, updatedAt, null);

        PaymentEntity paymentEntity = mapper.toEntity(payment);

        assertNotNull(paymentEntity);
        assertEquals(ID, paymentEntity.getId());
        assertEquals(INVOICE_ID, paymentEntity.getInvoiceId());
        assertEquals(0, AMOUNT.amount().compareTo(paymentEntity.getAmount()));
        assertEquals(AMOUNT.currency().getCurrencyCode(), paymentEntity.getCurrency());
        assertEquals(METHOD, paymentEntity.getMethod());
        assertEquals(status, paymentEntity.getStatus());
        assertEquals(FIXED_CLOCK.instant(), paymentEntity.getCreatedAt());
        assertEquals(updatedAt, paymentEntity.getUpdatedAt());
        assertNull(paymentEntity.getCompletedAt());
    }

    @Test
    void should_map_created_entity_to_domain() {

        PaymentEntity paymentEntity = getEntityWithStatusCreated();

        Payment payment = mapper.toDomain(paymentEntity);

        assertNotNull(payment);
        assertEquals(ID, payment.getId());
        assertEquals(INVOICE_ID, payment.getInvoiceId());
        assertEquals(0, AMOUNT.compareTo(payment.getAmount()));
        assertEquals(METHOD, payment.getMethod());
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), payment.getCreatedAt());
        assertNull(payment.getUpdatedAt());
        assertNull(payment.getCompletedAt());
    }

    @Test
    void should_map_completed_entity_to_domain() {

        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(7);
        PaymentEntity paymentEntity = getEntityWithStatusCompleted(updatedAt);

        Payment payment = mapper.toDomain(paymentEntity);

        assertNotNull(payment);
        assertEquals(ID, payment.getId());
        assertEquals(INVOICE_ID, payment.getInvoiceId());
        assertEquals(0, AMOUNT.compareTo(payment.getAmount()));
        assertEquals(METHOD, payment.getMethod());
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), payment.getCreatedAt());
        assertEquals(updatedAt, payment.getUpdatedAt());
        assertEquals(updatedAt, payment.getCompletedAt());
    }

    @Test
    void should_throw_exception_when_payment_is_null() {

        assertThrows(PaymentMapperException.class,
                () -> mapper.toEntity(null));
    }

    @Test
    void should_throw_exception_when_entity_is_null() {

        assertThrows(PaymentMapperException.class,
                () -> mapper.toDomain(null));
    }

    @ParameterizedTest(name = "Test {index}: currency [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            " ",
            "   ",
            "eur",
            "INVALID"
    })
    void should_throw_exception_when_persisted_currency_is_invalid(String currency) {

        PaymentEntity entity = getEntityWithStatusCreated();
        entity.setCurrency(currency);

        assertThrows(InvalidPersistedCurrencyException.class,
                () -> mapper.toDomain(entity));
    }

    @Test
    void should_normalize_persisted_currency_code() {

        PaymentEntity entity = getEntityWithStatusCreated();
        entity.setCurrency("    EUR  ");

        Payment payment = mapper.toDomain(entity);

        assertEquals(CURRENCY, payment.getAmount().currency());
    }

    @Test
    void should_throw_reconstitution_exception_when_entity_contains_invalid_domain_state() {

        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(7);
        PaymentEntity paymentEntity = getEntityWithStatusCompleted(updatedAt);
        paymentEntity.setCompletedAt(null);

        assertThrows(PaymentReconstitutionException.class,
                () -> mapper.toDomain(paymentEntity));
    }

    @Test
    void should_preserve_payment_data_during_round_trip_mapping() {

        PaymentStatus status = PaymentStatus.COMPLETED;
        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(7);

        Payment payment = getReconstitutedPayment(status, updatedAt, updatedAt);

        PaymentEntity paymentEntity = mapper.toEntity(payment);

        Payment reconstitutedPayment = mapper.toDomain(paymentEntity);

        assertNotNull(reconstitutedPayment);
        assertEquals(payment.getId(), reconstitutedPayment.getId());
        assertEquals(payment.getInvoiceId(), reconstitutedPayment.getInvoiceId());
        assertEquals(payment.getStatus(), reconstitutedPayment.getStatus());
        assertEquals(payment.getMethod(), reconstitutedPayment.getMethod());
        assertEquals(0, payment.getAmount().compareTo(reconstitutedPayment.getAmount()));
        assertEquals(payment.getCreatedAt(), reconstitutedPayment.getCreatedAt());
        assertEquals(payment.getUpdatedAt(), reconstitutedPayment.getUpdatedAt());
        assertEquals(payment.getCompletedAt(), reconstitutedPayment.getCompletedAt());
    }

    private PaymentEntity getEntityWithStatusCreated() {

        PaymentEntity entity = new PaymentEntity();

        entity.setId(ID);
        entity.setInvoiceId(INVOICE_ID);
        entity.setAmount(AMOUNT.amount());
        entity.setCurrency(CURRENCY.getCurrencyCode());
        entity.setStatus(PaymentStatus.CREATED);
        entity.setMethod(METHOD);
        entity.setCreatedAt(FIXED_CLOCK.instant());

        return entity;
    }

    private PaymentEntity getEntityWithStatusCompleted(Instant updatedAt) {

        PaymentEntity entity = getEntityWithStatusCreated();

        entity.setStatus(PaymentStatus.COMPLETED);
        entity.setUpdatedAt(updatedAt);
        entity.setCompletedAt(updatedAt);

        return entity;
    }

    private Payment getReconstitutedPayment(
            PaymentStatus status,
            Instant updatedAt,
            Instant completedAt
    ) {
        return Payment.reconstitute(
                ID,
                INVOICE_ID,
                AMOUNT,
                METHOD,
                status,
                FIXED_CLOCK.instant(),
                updatedAt,
                completedAt
        );
    }

    private Payment getCreatedPayment() {
        return new Payment(
                INVOICE_ID,
                AMOUNT,
                METHOD,
                FIXED_CLOCK.instant());
    }
}