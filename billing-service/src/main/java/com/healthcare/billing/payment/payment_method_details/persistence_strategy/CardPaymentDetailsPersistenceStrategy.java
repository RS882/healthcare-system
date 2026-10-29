package com.healthcare.billing.payment.payment_method_details.persistence_strategy;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.persistence.repository.CardPaymentDetailsRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CardPaymentDetailsPersistenceStrategy
        extends AbstractPaymentDetailsPersistenceStrategy {

    private final CardPaymentDetailsRepositoryAdapter cardAdapter;

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public void save(PaymentMethodDetails details, Long paymentId) {

        validateId(paymentId);
        validateDetails(details);

        if (details instanceof CardPaymentMethodDetails cardDetails) {
            cardAdapter.save(cardDetails, paymentId);

        } else {
            throw new PaymentDetailsValidationException("Payment method details type mismatch");
        }
    }

    @Override
    public Optional<PaymentMethodDetails> findByPaymentId(Long paymentId) {

        validateId(paymentId);

        return cardAdapter.findByPaymentId(paymentId)
                .map(details -> (PaymentMethodDetails) details);
    }
}
