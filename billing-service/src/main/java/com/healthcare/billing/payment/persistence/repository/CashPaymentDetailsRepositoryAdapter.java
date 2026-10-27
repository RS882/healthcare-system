package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentDetailsJpaAdapterException;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.persistence.entity.CashPaymentDetailsEntity;
import com.healthcare.billing.payment.persistence.mapper.CashPaymentDetailsMapper;
import com.healthcare.billing.validation.IdValidation;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CashPaymentDetailsRepositoryAdapter {

    private final CashPaymentDetailsMapper mapper;
    private final JpaCashPaymentDetailsRepository repository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(
            CashPaymentMethodDetails details,
            Long paymentId
    ) {
        validateDetails(details);
        validateId(paymentId);

        CashPaymentDetailsEntity entity = mapper.toEntity(details, paymentId);

        entityManager.persist(entity);
    }

    public Optional<CashPaymentMethodDetails> findByPaymentId(
            Long paymentId
    ) {
        validateId(paymentId);

        return repository.findById(paymentId)
                .map(mapper::toDomain);
    }

    private void validateId(Long paymentId) {

        IdValidation.validateId(paymentId, "Payment id", PaymentDetailsJpaAdapterException::new);

    }

    private void validateDetails(CashPaymentMethodDetails details) {

        if (details == null) {
            throw new PaymentDetailsJpaAdapterException("Cash payment method details is null");
        }
    }
}
