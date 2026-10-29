package com.healthcare.billing.payment.payment_method_details;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.PaymentDetailsAdapterRegistry;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.PaymentDetailsPersistenceStrategy;
import com.healthcare.billing.validation.IdValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentMethodDetailsPersistenceAdapter implements PaymentMethodDetailsPort {

    private final PaymentDetailsAdapterRegistry registry;

    @Override
    public void savePaymentMethodDetails(Long paymentId, PaymentMethodDetails details) {

        validateId(paymentId);
        validateDetails(details);

        PaymentDetailsPersistenceStrategy strategy =
                registry.getStrategy(details.getPaymentMethod());

        strategy.save(details, paymentId);
    }

    @Override
    public Optional<PaymentMethodDetails> getPaymentMethodDetails(Long paymentId, PaymentMethod method) {

        validateId(paymentId);
        validateMethod(method);

        PaymentDetailsPersistenceStrategy strategy = registry.getStrategy(method);

        return strategy.findByPaymentId(paymentId);
    }

    private void validateId(Long paymentId) {

        IdValidation.validateId(
                paymentId,
                "Payment id",
                PaymentDetailsValidationException::new);
    }

    private void validateDetails(PaymentMethodDetails details) {
        if (details == null) {
            throw new PaymentDetailsValidationException("Payment method details is null");
        }
    }

    private void validateMethod(PaymentMethod method) {
        if (method == null) {
            throw new PaymentDetailsValidationException("Payment method is null");
        }
    }
}
