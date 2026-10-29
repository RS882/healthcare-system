package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.persistence.repository.CashPaymentDetailsRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CashPaymentDetailsPersistenceStrategy
        extends AbstractPaymentDetailsPersistenceStrategy {

    private final CashPaymentDetailsRepositoryAdapter cashAdapter;

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public void save(PaymentMethodDetails details, Long paymentId) {

        validateId(paymentId);
        validateDetails(details);

        if (details instanceof CashPaymentMethodDetails cashDetails) {
            cashAdapter.save(cashDetails, paymentId);

        } else {
            throw new PaymentDetailsValidationException("Payment method details type mismatch");
        }
    }

    @Override
    public Optional<PaymentMethodDetails> findByPaymentId(Long paymentId) {

        validateId(paymentId);

        return cashAdapter.findByPaymentId(paymentId)
                .map(details -> (PaymentMethodDetails) details);
    }
}
