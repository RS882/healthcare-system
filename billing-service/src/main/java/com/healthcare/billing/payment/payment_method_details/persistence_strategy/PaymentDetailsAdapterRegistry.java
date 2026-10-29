package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentDetailsAdapterRegistry {

    private final Map<PaymentMethod, PaymentDetailsPersistenceStrategy> strategies;

    public PaymentDetailsAdapterRegistry(List<PaymentDetailsPersistenceStrategy> strategyList) {

        validateStrategyList(strategyList);

        this.strategies = Map.copyOf(
                strategyList.stream()
                        .collect(Collectors.toMap(
                                PaymentDetailsPersistenceStrategy::supportedMethod,
                                strategy -> strategy,
                                (existing, duplicate) -> {
                                    throw new IllegalStateException(
                                            "Duplicate payment details strategy: %s and %s"
                                                    .formatted(
                                                            existing.getClass().getName(),
                                                            duplicate.getClass().getName()
                                                    )
                                    );
                                }
                        ))
        );
    }

    public PaymentDetailsPersistenceStrategy getStrategy(PaymentMethod method) {

        validateMethod(method);

        PaymentDetailsPersistenceStrategy strategy = strategies.get(method);

        if (strategy == null) {
            throw new PaymentDetailsValidationException(
                    "No payment details strategy registered for method: " + method
            );
        }

        return strategy;
    }

    private void validateStrategyList(List<PaymentDetailsPersistenceStrategy> strategyList) {

        if (strategyList == null) {
            throw new PaymentDetailsValidationException("Strategy list can't be null");
        }

        if (strategyList.isEmpty()) {
            throw new PaymentDetailsValidationException("Strategy list can't be empty");
        }

        for (PaymentDetailsPersistenceStrategy strategy : strategyList) {
            if (strategy == null) {
                throw new IllegalStateException("Strategy list contains null");
            }

            if (strategy.supportedMethod() == null) {
                throw new IllegalStateException("Strategy supported method is null");
            }
        }
    }

    private void validateMethod(PaymentMethod method) {

        if (method == null) {
            throw new PaymentDetailsValidationException("Strategy supported method is null");
        }
    }
}
