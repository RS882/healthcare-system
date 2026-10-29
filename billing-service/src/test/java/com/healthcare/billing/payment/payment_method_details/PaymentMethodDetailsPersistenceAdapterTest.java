package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.PaymentDetailsAdapterRegistry;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.PaymentDetailsPersistenceStrategy;
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
class PaymentMethodDetailsPersistenceAdapterTest {

    @Mock
    private PaymentDetailsAdapterRegistry registry;

    @Mock
    private PaymentDetailsPersistenceStrategy strategy;

    @InjectMocks
    private PaymentMethodDetailsPersistenceAdapter adapter;

    private static final Long PAYMENT_ID = 12L;

    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.ADYEN;
    private static final String PROVIDER_TRANSACTION_ID = "Test transaction ID";

    private static final Money MONEY_AMOUNT = Money.of(
            new BigDecimal("328.29"),
            Currency.getInstance("EUR")
    );

    @Test
    void savePaymentMethodDetails_should_select_card_strategy_and_call_save_when_method_is_card() {

        CardPaymentMethodDetails details = getCardDetails();

        when(registry.getStrategy(PaymentMethod.CARD)).thenReturn(strategy);

        adapter.savePaymentMethodDetails(PAYMENT_ID, details);

        verify(registry).getStrategy(PaymentMethod.CARD);
        verify(strategy).save(details, PAYMENT_ID);
    }

    @Test
    void savePaymentMethodDetails_should_select_cash_strategy_and_call_save_when_method_is_cash() {

        CashPaymentMethodDetails details = getCashDetails();

        when(registry.getStrategy(PaymentMethod.CASH)).thenReturn(strategy);

        adapter.savePaymentMethodDetails(PAYMENT_ID, details);

        verify(registry).getStrategy(PaymentMethod.CASH);
        verify(strategy).save(details, PAYMENT_ID);
    }

    @Test
    void savePaymentMethodDetails_should_return_exception_when_details_is_null() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> adapter.savePaymentMethodDetails(PAYMENT_ID, null));

        verifyNoInteractions(registry, strategy);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L,
            Long.MIN_VALUE
    })
    void savePaymentMethodDetails_should_return_exception_when_payment_id_is_invalid(Long id) {

        PaymentMethodDetails details = getCashDetails();

        assertThrows(PaymentDetailsValidationException.class,
                () -> adapter.savePaymentMethodDetails(id, details));

        verifyNoInteractions(registry, strategy);
    }

    @Test
    void getPaymentMethodDetails_should_return_correct_card_details() {
        CardPaymentMethodDetails details = getCardDetails();

        when(registry.getStrategy(PaymentMethod.CARD)).thenReturn(strategy);
        when(strategy.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(details));

        Optional<PaymentMethodDetails> result =
                adapter.getPaymentMethodDetails(PAYMENT_ID, PaymentMethod.CARD);

        assertTrue(result.isPresent());
        assertInstanceOf(CardPaymentMethodDetails.class, result.orElseThrow());
        assertSame(details, result.orElseThrow());

        verify(registry).getStrategy(PaymentMethod.CARD);
        verify(strategy).findByPaymentId(PAYMENT_ID);
    }

    @Test
    void getPaymentMethodDetails_should_return_correct_cash_details() {
        CashPaymentMethodDetails details = getCashDetails();

        when(registry.getStrategy(PaymentMethod.CASH)).thenReturn(strategy);
        when(strategy.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(details));

        Optional<PaymentMethodDetails> result =
                adapter.getPaymentMethodDetails(PAYMENT_ID, PaymentMethod.CASH);

        assertTrue(result.isPresent());
        assertInstanceOf(CashPaymentMethodDetails.class, result.orElseThrow());
        assertSame(details, result.orElseThrow());

        verify(registry).getStrategy(PaymentMethod.CASH);
        verify(strategy).findByPaymentId(PAYMENT_ID);
    }

    @Test
    void getPaymentMethodDetails_should_return_optional_empty_when_details_not_found() {

        when(registry.getStrategy(PaymentMethod.CASH)).thenReturn(strategy);
        when(strategy.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        Optional<PaymentMethodDetails> result =
                adapter.getPaymentMethodDetails(PAYMENT_ID, PaymentMethod.CASH);

        assertFalse(result.isPresent());

        verify(registry).getStrategy(PaymentMethod.CASH);
        verify(strategy).findByPaymentId(PAYMENT_ID);
    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L,
            Long.MIN_VALUE
    })
    void getPaymentMethodDetails_should_return_exception_when_payment_id_is_invalid(Long id) {

        assertThrows(PaymentDetailsValidationException.class,
                () -> adapter.getPaymentMethodDetails(id, PaymentMethod.CASH));

        verifyNoInteractions(registry, strategy);
    }

    @Test
    void getPaymentMethodDetails_should_return_exception_when_method_is_null() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> adapter.getPaymentMethodDetails(PAYMENT_ID, null));

        verifyNoInteractions(registry, strategy);
    }

    @Test
    void getPaymentMethodDetails_should_return_exception_when_registry_do_not_find_strategy() {

        String errorMessage = "Strategy not found";

        when(registry.getStrategy(PaymentMethod.CARD))
                .thenThrow(new PaymentDetailsValidationException(errorMessage));

        PaymentDetailsValidationException exception =
                assertThrows(PaymentDetailsValidationException.class,
                        () -> adapter.getPaymentMethodDetails(PAYMENT_ID, PaymentMethod.CARD));

        assertEquals(errorMessage, exception.getMessage());

        verify(registry).getStrategy(PaymentMethod.CARD);
        verifyNoInteractions(strategy);
    }

    @Test
    void savePaymentMethodDetails_should_return_exception_when_strategy_returns_exception() {

        String errorMessage = "Error";

        CardPaymentMethodDetails details = getCardDetails();

        when(registry.getStrategy(PaymentMethod.CARD)).thenReturn(strategy);

        doThrow(new PaymentDetailsValidationException(errorMessage))
                .when(strategy).save(details, PAYMENT_ID);

        PaymentDetailsValidationException exception =
                assertThrows(PaymentDetailsValidationException.class,
                () -> adapter.savePaymentMethodDetails(PAYMENT_ID, details));

        assertEquals(errorMessage, exception.getMessage());

        verify(registry).getStrategy(PaymentMethod.CARD);
        verify(strategy).save(details, PAYMENT_ID);
    }

    @Test
    void getPaymentMethodDetails_should_return_exception_when_strategy_returns_exception() {

        String errorMessage = "Error";

        when(registry.getStrategy(PaymentMethod.CARD)).thenReturn(strategy);

        when(strategy.findByPaymentId(PAYMENT_ID))
                .thenThrow(new PaymentDetailsValidationException(errorMessage));


        PaymentDetailsValidationException exception =
                assertThrows(PaymentDetailsValidationException.class,
                        () -> adapter.getPaymentMethodDetails(PAYMENT_ID, PaymentMethod.CARD));

        assertEquals(errorMessage, exception.getMessage());

        verify(registry).getStrategy(PaymentMethod.CARD);
        verify(strategy).findByPaymentId(PAYMENT_ID);
    }

    private CardPaymentMethodDetails getCardDetails() {

        return new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);
    }

    private CashPaymentMethodDetails getCashDetails() {

        return new CashPaymentMethodDetails(MONEY_AMOUNT);
    }
}