package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentDetailsJpaAdapterException;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import com.healthcare.billing.payment.persistence.entity.CardPaymentDetailsEntity;
import com.healthcare.billing.payment.persistence.mapper.CardPaymentDetailsMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CardPaymentDetailsRepositoryAdapterTest {

    @Mock
    private CardPaymentDetailsMapper mapper;

    @Mock
    private JpaCardPaymentDetailsRepository repository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CardPaymentDetailsRepositoryAdapter adapter;

    private static final Long PAYMENT_ID = 97L;
    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.ADYEN;
    private static final String PROVIDER_TRANSACTION_ID = "Test transaction ID";

    @Test
    void save_should_persist_card_details() {

        CardPaymentMethodDetails details =
                new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);

        CardPaymentDetailsEntity entity = new CardPaymentDetailsEntity(
                PAYMENT_ID,
                PROVIDER,
                PROVIDER_TRANSACTION_ID
        );

        when(mapper.toEntity(details, PAYMENT_ID)).thenReturn(entity);

        adapter.save(details, PAYMENT_ID);

        verify(mapper).toEntity(details, PAYMENT_ID);
        verify(entityManager).persist(entity);
        verifyNoInteractions(repository);
    }

    @Test
    void save_should_reject_null_details() {

        assertThrows(PaymentDetailsJpaAdapterException.class,
                () -> adapter.save(null, PAYMENT_ID));

        verifyNoInteractions(mapper, entityManager, repository);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void save_should_reject_null_or_invalid_payment_id(Long id) {

        CardPaymentMethodDetails details =
                new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);

        assertThrows(PaymentDetailsJpaAdapterException.class,
                () -> adapter.save(details, id));

        verifyNoInteractions(mapper, entityManager, repository);
    }

    @Test
    void findByPaymentId_should_return_details() {

        CardPaymentDetailsEntity entity = new CardPaymentDetailsEntity(
                PAYMENT_ID,
                PROVIDER,
                PROVIDER_TRANSACTION_ID
        );

        CardPaymentMethodDetails details =
                new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);

        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(details);

        CardPaymentMethodDetails result = adapter.findByPaymentId(PAYMENT_ID)
                .orElseThrow();

        assertEquals(details, result);
        assertEquals(PROVIDER_TRANSACTION_ID, result.providerTransactionId());
        assertEquals(PROVIDER, result.provider());

        verify(repository).findById(PAYMENT_ID);
        verify(mapper).toDomain(entity);
        verifyNoInteractions(entityManager);
    }

    @Test
    void findByPaymentId_should_return_empty_when_not_found() {

        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        Optional<CardPaymentMethodDetails> result = adapter.findByPaymentId(PAYMENT_ID);

        assertFalse(result.isPresent());
        verify(repository).findById(PAYMENT_ID);
        verifyNoInteractions(entityManager, mapper);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void findByPaymentId_should_reject_invalid_id(Long id) {

        assertThrows(PaymentDetailsJpaAdapterException.class,
                () -> adapter.findByPaymentId(id));

        verifyNoInteractions(mapper, entityManager, repository);
    }
}