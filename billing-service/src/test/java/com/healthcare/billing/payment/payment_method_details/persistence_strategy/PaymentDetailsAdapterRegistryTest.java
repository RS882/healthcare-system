package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PaymentDetailsAdapterRegistryTest {

    @Mock
    private PaymentDetailsPersistenceStrategy cardStrategy;

    @Mock
    private PaymentDetailsPersistenceStrategy cashStrategy;

    @Test
    void should_return_correct_strategy() {

        List<PaymentDetailsPersistenceStrategy> strategyList = getStrategyList();

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(PaymentMethod.CASH);

        PaymentDetailsAdapterRegistry registry = new PaymentDetailsAdapterRegistry(strategyList);

        assertSame(cardStrategy, registry.getStrategy(PaymentMethod.CARD));
        assertSame(cashStrategy, registry.getStrategy(PaymentMethod.CASH));
    }

    @Test
    void should_return_exception_when_supported_methods_are_identical() {

        List<PaymentDetailsPersistenceStrategy> strategyList = getStrategyList();

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);

        assertThrows(IllegalStateException.class,
                () -> new PaymentDetailsAdapterRegistry(strategyList));
    }

    @Test
    void should_return_exception_when_strategy_list_is_null() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentDetailsAdapterRegistry(null));
    }

    @Test
    void should_return_exception_when_strategy_list_is_empty() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> new PaymentDetailsAdapterRegistry(List.of()));
    }

    @Test
    void should_return_exception_when_strategy_list_contains_null() {

        assertThrows(IllegalStateException.class,
                () -> new PaymentDetailsAdapterRegistry(Arrays.asList(null, cardStrategy)));
    }

    @Test
    void should_return_exception_when_strategy_supported_method_is_null() {

        List<PaymentDetailsPersistenceStrategy> strategyList = getStrategyList();

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> new PaymentDetailsAdapterRegistry(strategyList));
    }

    @Test
    void should_return_exception_when_argument_in_getStrategy_is_null() {

        List<PaymentDetailsPersistenceStrategy> strategyList = getStrategyList();

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(PaymentMethod.CASH);

        PaymentDetailsAdapterRegistry registry = new PaymentDetailsAdapterRegistry(strategyList);

        assertThrows(PaymentDetailsValidationException.class,
                () -> registry.getStrategy(null));
    }

    @Test
    void should_return_exception_when_payment_method_not_registered() {

        List<PaymentDetailsPersistenceStrategy> strategyList = getStrategyList();

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(PaymentMethod.CASH);

        PaymentDetailsAdapterRegistry registry = new PaymentDetailsAdapterRegistry(strategyList);

        assertThrows(PaymentDetailsValidationException.class,
                () -> registry.getStrategy(PaymentMethod.BANK_TRANSFER));
    }

    @Test
    void should_return_correct_strategy_when_procedure_for_registering_strategies_changed() {

        List<PaymentDetailsPersistenceStrategy> strategyList = new ArrayList<>();
        strategyList.add(cashStrategy);
        strategyList.add(cardStrategy);

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(PaymentMethod.CASH);

        PaymentDetailsAdapterRegistry registry = new PaymentDetailsAdapterRegistry(strategyList);

        assertSame(cardStrategy, registry.getStrategy(PaymentMethod.CARD));
        assertSame(cashStrategy, registry.getStrategy(PaymentMethod.CASH));
    }

    @Test
    void should_not_change_registered_strategies_when_source_list_changes() {

        List<PaymentDetailsPersistenceStrategy> strategyList = getStrategyList();

        when(cardStrategy.supportedMethod()).thenReturn(PaymentMethod.CARD);
        when(cashStrategy.supportedMethod()).thenReturn(PaymentMethod.CASH);

        PaymentDetailsAdapterRegistry registry = new PaymentDetailsAdapterRegistry(strategyList);

        strategyList.clear();

        assertSame(cardStrategy, registry.getStrategy(PaymentMethod.CARD));

        assertSame(cashStrategy, registry.getStrategy(PaymentMethod.CASH));
    }

    private List<PaymentDetailsPersistenceStrategy> getStrategyList() {

        List<PaymentDetailsPersistenceStrategy> strategyList = new ArrayList<>();
        strategyList.add(cardStrategy);
        strategyList.add(cashStrategy);

        return strategyList;
    }
}




