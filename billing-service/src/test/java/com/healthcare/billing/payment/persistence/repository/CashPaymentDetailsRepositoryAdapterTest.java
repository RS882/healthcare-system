package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentDetailsJpaAdapterException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.persistence.entity.CashPaymentDetailsEntity;
import com.healthcare.billing.payment.persistence.mapper.CashPaymentDetailsMapper;
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

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashPaymentDetailsRepositoryAdapterTest {

    @Mock
    private CashPaymentDetailsMapper mapper;

    @Mock
    private JpaCashPaymentDetailsRepository repository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CashPaymentDetailsRepositoryAdapter adapter;

    private static final Long PAYMENT_ID = 97L;
    private static final String EUR = "EUR";
    private static final Currency CURRENCY_EUR = Currency.getInstance(EUR);
    private static final BigDecimal AMOUNT = new BigDecimal("328.29");
    private static final Money MONEY_AMOUNT = Money.of(AMOUNT, CURRENCY_EUR);

    @Test
    void save_should_persist_mapped_entity() {

        CashPaymentMethodDetails details = new CashPaymentMethodDetails(MONEY_AMOUNT);

        CashPaymentDetailsEntity entity = new CashPaymentDetailsEntity(PAYMENT_ID, AMOUNT, EUR);

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
            -726L,
            Long.MIN_VALUE
    })
    void save_should_reject_invalid_payment_id(Long id) {

        CashPaymentMethodDetails details = new CashPaymentMethodDetails(MONEY_AMOUNT);

        assertThrows(PaymentDetailsJpaAdapterException.class,
                () -> adapter.save(details, id));

        verifyNoInteractions(mapper, entityManager, repository);
    }

    @Test
    void save_should_not_persist_when_mapper_fails() {

        CashPaymentMethodDetails details = new CashPaymentMethodDetails(MONEY_AMOUNT);
        String errorMessage = "Error";

        when(mapper.toEntity(details, PAYMENT_ID))
                .thenThrow(new PaymentDetailsValidationException(errorMessage));

        PaymentDetailsValidationException exception = assertThrows(PaymentDetailsValidationException.class,
                () -> adapter.save(details, PAYMENT_ID));

        assertEquals(errorMessage, exception.getMessage());

        verify(mapper).toEntity(details, PAYMENT_ID);
        verifyNoInteractions(entityManager, repository);
    }

    @Test
    void findByPaymentId_should_return_details_when_found() {

        CashPaymentMethodDetails details = new CashPaymentMethodDetails(MONEY_AMOUNT);

        CashPaymentDetailsEntity entity = new CashPaymentDetailsEntity(PAYMENT_ID, AMOUNT, EUR);

        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(details);

        CashPaymentMethodDetails result = adapter.findByPaymentId(PAYMENT_ID)
                .orElseThrow();

        assertEquals(details, result);
        assertEquals(MONEY_AMOUNT, result.receivedAmount());

        verify(repository).findById(PAYMENT_ID);
        verify(mapper).toDomain(entity);
        verifyNoInteractions(entityManager);
    }

    @Test
    void findByPaymentId_should_return_empty_when_not_found() {

        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        Optional<CashPaymentMethodDetails> result = adapter.findByPaymentId(PAYMENT_ID);

        assertFalse(result.isPresent());
        verify(repository).findById(PAYMENT_ID);
        verifyNoInteractions(entityManager, mapper);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L,
            Long.MIN_VALUE
    })
    void findByPaymentId_should_reject_invalid_payment_id(Long id) {

        assertThrows(PaymentDetailsJpaAdapterException.class,
                () -> adapter.findByPaymentId(id));

        verifyNoInteractions(mapper, entityManager, repository);
    }
}
