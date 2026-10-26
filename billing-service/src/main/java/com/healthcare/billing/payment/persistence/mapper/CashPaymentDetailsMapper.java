package com.healthcare.billing.payment.persistence.mapper;

import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.persistence.entity.CashPaymentDetailsEntity;
import com.healthcare.billing.validation.IdValidation;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CashPaymentDetailsMapper {

    public CashPaymentDetailsEntity toEntity(
            CashPaymentMethodDetails details,
            Long paymentId
    ) {

        validateId(paymentId);
        validateDetails(details);

        return new CashPaymentDetailsEntity(
                paymentId,
                details.receivedAmount().amount(),
                details.receivedAmount().currency().getCurrencyCode()
        );
    }

    public CashPaymentMethodDetails toDomain(
            CashPaymentDetailsEntity entity) {

        validateEntity(entity);

        Money amount = Money.of(
                entity.getReceivedAmount(),
                resolveCurrency(entity.getCurrency())
        );

        return new CashPaymentMethodDetails(amount);
    }

    private Currency resolveCurrency(String currencyCode) {

        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidPersistedCurrencyException(currencyCode);
        }

        try {
            return Currency.getInstance(currencyCode.strip());
        } catch (IllegalArgumentException exception) {
            throw new InvalidPersistedCurrencyException(currencyCode);
        }
    }

    private void validateEntity(CashPaymentDetailsEntity entity) {

        if (entity == null) {
            throw new PaymentDetailsValidationException("Cash payment details entity is null");
        }
    }

    private void validateDetails(CashPaymentMethodDetails details) {

        if (details == null) {
            throw new PaymentDetailsValidationException("Cash payment method details is null");
        }
    }

    private void validateId(Long paymentId) {

        if (!IdValidation.isValid(paymentId)) {
            throw new PaymentDetailsValidationException(
                    "Payment ID is invalid. Provided value: %s".formatted(paymentId)
            );
        }
    }
}
