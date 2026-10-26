package com.healthcare.billing.payment.persistence.mapper;

import com.healthcare.billing.exception.PaymentDetailsJpaAdapterException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.persistence.entity.CardPaymentDetailsEntity;
import com.healthcare.billing.validation.IdValidation;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentDetailsMapper {

    public CardPaymentDetailsEntity toEntity(
            CardPaymentMethodDetails details,
            Long paymentId) {

        validateDetails(details);
        validateId(paymentId);

        return new CardPaymentDetailsEntity(
                paymentId,
                details.provider(),
                details.providerTransactionId()
        );
    }

    public CardPaymentMethodDetails toDomain(CardPaymentDetailsEntity entity) {

        validateEntity(entity);

        return new CardPaymentMethodDetails(
                entity.getProviderTransactionId(),
                entity.getProvider()
        );
    }

    private void validateDetails(CardPaymentMethodDetails details) {

        if (details == null) {
            throw new PaymentDetailsValidationException("Card payment method details is null");
        }
    }

    private void validateEntity(CardPaymentDetailsEntity entity) {

        if (entity == null) {
            throw new PaymentDetailsValidationException("Card payment details entity is null");
        }
    }

    private void validateId(Long paymentId) {

        IdValidation.validateId(paymentId, "Payment id", PaymentDetailsValidationException::new);

    }
}
