package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentJpaAdapterException;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import com.healthcare.billing.payment.persistence.mapper.PaymentPersistenceMapper;
import com.healthcare.billing.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Payment save(Payment payment) {

        validatePayment(payment);

        PaymentEntity entity = mapper.toEntity(payment);

        PaymentEntity savedEntity = jpaPaymentRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(Long id) {

        validateId(id, "Payment ID");

        return jpaPaymentRepository
                .findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Payment> findByInvoiceId(Long invoiceId) {

        validateId(invoiceId, "Invoice ID");

        return jpaPaymentRepository
                .findByInvoiceId(invoiceId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new PaymentJpaAdapterException("Payment cannot be null");
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null) {
            throw new PaymentJpaAdapterException("%s cannot be null".formatted(fieldName));
        }
        if (id <= 0) {
            throw new PaymentJpaAdapterException("%s must be greater than zero".formatted(fieldName));
        }
    }
}
