package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.exception.PaymentDetailsJpaAdapterException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import com.healthcare.billing.payment.persistence.repository.CashPaymentDetailsRepositoryAdapter;
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
class CashPaymentDetailsPersistenceStrategyTest {

    @Mock
    private CashPaymentDetailsRepositoryAdapter adapter;

    @InjectMocks
    private CashPaymentDetailsPersistenceStrategy strategy;

    private static final Long PAYMENT_ID = 12L;

    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.ADYEN;
    private static final String PROVIDER_TRANSACTION_ID = "Test transaction ID";

    private static final Money MONEY_AMOUNT = Money.of(
            new BigDecimal("328.29"),
            Currency.getInstance("EUR")
    );

    @Test
    void supportedMethod_should_return_correct_method() {

        assertEquals(PaymentMethod.CASH, strategy.supportedMethod());

        verifyNoInteractions(adapter);
    }

    @Test
    void save_should_save_correct_details() {

        CashPaymentMethodDetails details =
                new CashPaymentMethodDetails(MONEY_AMOUNT);

        strategy.save(details, PAYMENT_ID);

        verify(adapter).save(details, PAYMENT_ID);
    }

    @Test
    void save_should_return_exception_when_details_have_another_type() {

        PaymentMethodDetails details =
                new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);

        assertThrows(PaymentDetailsValidationException.class,
                () -> strategy.save(details, PAYMENT_ID));

        verifyNoInteractions(adapter);
    }

    @Test
    void save_should_return_exception_when_details_is_null() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> strategy.save(null, PAYMENT_ID));

        verifyNoInteractions(adapter);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L,
            Long.MIN_VALUE
    })
    void save_should_return_exception_when_payment_id_is_invalid(Long id) {

        CashPaymentMethodDetails details =
                new CashPaymentMethodDetails(MONEY_AMOUNT);

        assertThrows(PaymentDetailsValidationException.class,
                () -> strategy.save(details, id));

        verifyNoInteractions(adapter);
    }

    @Test
    void findByPaymentId_should_return_correct_optional_details() {

        CashPaymentMethodDetails details =
                new CashPaymentMethodDetails(MONEY_AMOUNT);

        when(adapter.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(details));

        Optional<PaymentMethodDetails> result = strategy.findByPaymentId(PAYMENT_ID);

        assertTrue(result.isPresent());
        assertInstanceOf(CashPaymentMethodDetails.class, result.orElseThrow());
        assertSame(details, result.orElseThrow());

        verify(adapter).findByPaymentId(PAYMENT_ID);
    }

    @Test
    void findByPaymentId_should_return_optional_empty_when_no_details() {

        when(adapter.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        Optional<PaymentMethodDetails> result = strategy.findByPaymentId(PAYMENT_ID);

        assertFalse(result.isPresent());

        verify(adapter).findByPaymentId(PAYMENT_ID);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L,
            Long.MIN_VALUE
    })
    void findByPaymentId_should_return_exception_when_payment_id_is_invalid(Long id) {

        assertThrows(PaymentDetailsValidationException.class,
                () -> strategy.findByPaymentId(id));

        verifyNoInteractions(adapter);
    }

    @Test
    void save_should_return_exception_when_adapter_returns_exception() {

        String errorMsg = "Error message";

        CashPaymentMethodDetails details =
                new CashPaymentMethodDetails(MONEY_AMOUNT);

        doThrow(new PaymentDetailsJpaAdapterException(errorMsg))
                .when(adapter).save(details, PAYMENT_ID);


        PaymentDetailsJpaAdapterException exception =
                assertThrows(PaymentDetailsJpaAdapterException.class,
                        () -> strategy.save(details, PAYMENT_ID));

        assertEquals(errorMsg, exception.getMessage());

        verify(adapter).save(details, PAYMENT_ID);
    }

    @Test
    void findByPaymentId_should_return_exception_when_adapter_returns_exception() {

        String errorMsg = "Error message";

        when(adapter.findByPaymentId(PAYMENT_ID))
                .thenThrow(new PaymentDetailsJpaAdapterException(errorMsg));

        PaymentDetailsJpaAdapterException exception =
                assertThrows(PaymentDetailsJpaAdapterException.class,
                        () -> strategy.findByPaymentId(PAYMENT_ID));

        assertEquals(errorMsg, exception.getMessage());

        verify(adapter).findByPaymentId(PAYMENT_ID);
    }
}