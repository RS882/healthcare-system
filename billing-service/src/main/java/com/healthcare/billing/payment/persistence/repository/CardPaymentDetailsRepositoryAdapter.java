package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentDetailsJpaAdapterException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.persistence.entity.CardPaymentDetailsEntity;
import com.healthcare.billing.payment.persistence.mapper.CardPaymentDetailsMapper;
import com.healthcare.billing.validation.IdValidation;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardPaymentDetailsRepositoryAdapter {

    private final CardPaymentDetailsMapper mapper;
    private final JpaCardPaymentDetailsRepository repository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(
            CardPaymentMethodDetails details,
            Long paymentId) {

        validateDetails(details);
        validateId(paymentId);

        CardPaymentDetailsEntity entity = mapper.toEntity(details, paymentId);

        entityManager.persist(entity);
    }

    public Optional<CardPaymentMethodDetails> findByPaymentId(Long paymentId) {

        validateId(paymentId);

        return repository.findById(paymentId)
                .map(mapper::toDomain);
    }

    private void validateDetails(CardPaymentMethodDetails details) {

        if (details == null) {
            throw new PaymentDetailsJpaAdapterException("Card payment method details is null");
        }
    }

    private void validateId(Long paymentId) {

        IdValidation.validateId(paymentId, "Payment id", PaymentDetailsJpaAdapterException::new);

    }
}