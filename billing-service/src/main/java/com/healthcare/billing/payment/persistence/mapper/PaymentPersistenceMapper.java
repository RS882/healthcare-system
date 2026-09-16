package com.healthcare.billing.payment.persistence.mapper;

import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.exception.PaymentMapperException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class PaymentPersistenceMapper {

    public PaymentEntity toEntity(Payment payment) {

        if (payment == null) {
            throw new PaymentMapperException("Payment cannot be null");
        }

        PaymentEntity paymentEntity = new PaymentEntity();

        paymentEntity.setId(payment.getId());
        paymentEntity.setInvoiceId(payment.getInvoiceId());
        paymentEntity.setAmount(payment.getAmount().amount());
        paymentEntity.setCurrency(payment.getAmount().currency().getCurrencyCode());
        paymentEntity.setMethod(payment.getMethod());
        paymentEntity.setStatus(payment.getStatus());
        paymentEntity.setCreatedAt(payment.getCreatedAt());
        paymentEntity.setUpdatedAt(payment.getUpdatedAt());
        paymentEntity.setCompletedAt(payment.getCompletedAt());

        return paymentEntity;
    }

    public Payment toDomain(PaymentEntity entity) {

        if (entity == null) {
            throw new PaymentMapperException("Payment entity cannot be null");
        }

        Currency currency = resolveCurrency(entity.getCurrency());

        return Payment.reconstitute(
                entity.getId(),
                entity.getInvoiceId(),
                Money.of(entity.getAmount(), currency),
                entity.getMethod(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt()
        );
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
}
