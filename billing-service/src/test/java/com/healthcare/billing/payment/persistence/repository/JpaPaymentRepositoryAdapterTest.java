package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentJpaAdapterException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import com.healthcare.billing.payment.persistence.mapper.PaymentPersistenceMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JPA payment repository adapter tests: ")
class JpaPaymentRepositoryAdapterTest {

    @Mock
    private JpaPaymentRepository repository;

    @Mock
    private PaymentPersistenceMapper mapper;

    private JpaPaymentRepositoryAdapter adapter;

    private static final Long ID = 73L;
    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @BeforeEach
    void setUp() {

        adapter = new JpaPaymentRepositoryAdapter(repository, mapper);
    }

    @Test
    void should_return_saved_payment_when_payment_is_saved() {

        Payment createdPayment = getCreatedPayment();
        PaymentEntity createdEntity = getEntityWithStatusCreated();
        createdEntity.setId(null);

        PaymentEntity savedEntity = getEntityWithStatusCreated();
        Payment savedPayment = getReconstitutedPayment(
                PaymentStatus.CREATED,
                null,
                null
        );

        when(mapper.toEntity(createdPayment)).thenReturn(createdEntity);
        when(repository.save(createdEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedPayment);

        Payment result = adapter.save(createdPayment);

        assertSame(savedPayment, result);

        verify(mapper).toEntity(createdPayment);
        verify(mapper).toDomain(savedEntity);
        verify(repository).save(createdEntity);
        verify(mapper, never()).toDomain(createdEntity);
    }

    @Test
    void should_throw_exception_when_saving_null_payment() {
        assertThrows(PaymentJpaAdapterException.class,
                () -> adapter.save(null));

        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void should_return_payment_when_found_by_id() {

        Payment payment = getReconstitutedPayment(
                PaymentStatus.CREATED,
                null,
                null
        );
        PaymentEntity entity = getEntityWithStatusCreated();

        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(payment);

        Optional<Payment> result = adapter.findById(ID);

        assertTrue(result.isPresent());
        assertSame(payment, result.get());

        verify(repository).findById(ID);
        verify(mapper).toDomain(entity);
    }

    @Test
    void should_return_empty_optional_when_payment_is_not_found_by_id() {

        when(repository.findById(ID)).thenReturn(Optional.empty());

        Optional<Payment> result = adapter.findById(ID);

        assertFalse(result.isPresent());
        verifyNoInteractions(mapper);
        verify(repository).findById(ID);
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @NullSource
    @ValueSource(longs = {0L, -271L})
    void should_throw_exception_when_payment_id_is_invalid(Long id) {

        assertThrows(PaymentJpaAdapterException.class,
                () -> adapter.findById(id));

        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void should_return_payments_when_found_by_invoice_id() {

        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(7);
        Long id1 = 24L;
        Long id2 = 19L;
        Long id3 = 9517L;

        Payment payment1 = getReconstitutedPayment(
                id1,
                PaymentStatus.CREATED,
                null,
                null
        );

        Payment payment2 = getReconstitutedPayment(
                id2,
                PaymentStatus.PENDING,
                updatedAt,
                null
        );

        Payment payment3 = getReconstitutedPayment(
                id3,
                PaymentStatus.COMPLETED,
                updatedAt,
                updatedAt
        );

        PaymentEntity entity1 = getEntityWithStatusCreated();
        entity1.setId(id1);

        PaymentEntity entity2 = getEntityWithStatusCreated();
        entity2.setId(id2);
        entity2.setStatus(PaymentStatus.PENDING);
        entity2.setUpdatedAt(updatedAt);

        PaymentEntity entity3 = getEntityWithStatusCompleted(updatedAt);
        entity3.setId(id3);

        List<PaymentEntity> entities = List.of(entity1, entity2, entity3);

        when(repository.findByInvoiceId(INVOICE_ID)).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(payment1);
        when(mapper.toDomain(entity2)).thenReturn(payment2);
        when(mapper.toDomain(entity3)).thenReturn(payment3);

        List<Payment> result = adapter.findByInvoiceId(INVOICE_ID);

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        assertEquals(payment1, result.get(0));
        assertEquals(payment2, result.get(1));
        assertEquals(payment3, result.get(2));

        verify(repository).findByInvoiceId(INVOICE_ID);
        verify(mapper).toDomain(entity1);
        verify(mapper).toDomain(entity2);
        verify(mapper).toDomain(entity3);
    }

    @Test
    void should_return_empty_list_when_no_payments_found_by_invoice_id() {

        when(repository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of());
        List<Payment> result = adapter.findByInvoiceId(INVOICE_ID);

        assertTrue(result.isEmpty());
        verify(repository).findByInvoiceId(INVOICE_ID);
        verifyNoInteractions(mapper);
    }

    @ParameterizedTest(name = "Test {index}: invoice id [{arguments}]")
    @NullSource
    @ValueSource(longs = {0L, -271L})
    void should_throw_exception_when_invoice_id_is_invalid(Long id) {

        assertThrows(PaymentJpaAdapterException.class,
                () -> adapter.findByInvoiceId(id));

        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
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
        return getReconstitutedPayment(ID, status, updatedAt, completedAt);
    }

    private Payment getReconstitutedPayment(
            Long id,
            PaymentStatus status,
            Instant updatedAt,
            Instant completedAt
    ) {
        return Payment.reconstitute(
                id,
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