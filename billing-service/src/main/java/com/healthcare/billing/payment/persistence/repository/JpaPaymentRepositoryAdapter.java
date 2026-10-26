package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentJpaAdapterException;
import com.healthcare.billing.exception.PaymentNotFoundException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.persistence.dto.AmountDto;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import com.healthcare.billing.payment.persistence.mapper.PaymentPersistenceMapper;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.validation.IdValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentPersistenceMapper mapper;
    private final MoneyPolicy moneyPolicy;

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

    @Override
    public Money calculatePaidAmount(Long invoiceId) {

        validateId(invoiceId, "Invoice ID");

        List<AmountDto> amountDtoList = jpaPaymentRepository
                .calculatePaidAmountPerCurrency(
                        invoiceId, PaymentStatus.COMPLETED);

        validateAmountDtoList(amountDtoList, invoiceId);

        if (amountDtoList.isEmpty()) {
            return moneyPolicy.zero();
        }

        AmountDto amountDto = amountDtoList.get(0);

        validateAmountDto(amountDto);

        return moneyPolicy.moneyOf(amountDto.amount());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(Payment payment) {

        validatePayment(payment);

        Long paymentId = payment.getId();

        validateId(paymentId, "Payment ID");

        PaymentEntity entity = jpaPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        validatePaymentImmutableFields(payment, entity);

        mapper.updateEntity(payment, entity);
    }

    private void validatePaymentImmutableFields(Payment payment, PaymentEntity entity) {

        if (!Objects.equals(payment.getId(), entity.getId())) {
            throw new PaymentJpaAdapterException("Payment ID and payment entity ID do not match");
        }
        if (!Objects.equals(payment.getInvoiceId(), entity.getInvoiceId())) {
            throw new PaymentJpaAdapterException("Invoice IDs do not match");
        }
        if (payment.getAmount().amount().compareTo(entity.getAmount()) != 0) {
            throw new PaymentJpaAdapterException("Amount do not match");
        }
        if (!Objects.equals(
                payment.getAmount().currency().getCurrencyCode(),
                entity.getCurrency()
        )) {
            throw new PaymentJpaAdapterException("Currency code do not match");
        }
        if (payment.getMethod() != entity.getMethod()) {
            throw new PaymentJpaAdapterException("Method code do not match");
        }
        if (!Objects.equals(payment.getCreatedAt(), entity.getCreatedAt())) {
            throw new PaymentJpaAdapterException("Created at do not match");
        }
    }

    private void validateAmountDto(AmountDto amountDto) {

        if (amountDto == null) {
            throw new PaymentJpaAdapterException("Amount DTO is null");
        }

        validateAmount(amountDto.amount());

        validateCurrency(amountDto.currency());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new PaymentJpaAdapterException("Amount is null");
        }

        if (amount.signum() < 0) {
            throw new PaymentJpaAdapterException("Amount cannot be negative");
        }
    }

    private void validateCurrency(String currencyCode) {

        if (currencyCode == null) {
            throw new PaymentJpaAdapterException("Currency is null");
        }

        if (currencyCode.isBlank()) {
            throw new PaymentJpaAdapterException("Currency is blank");
        }

        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode.strip());
        } catch (IllegalArgumentException exception) {
            throw new PaymentJpaAdapterException("Invalid currency code: '%s'".formatted(currencyCode));
        }

        if (!moneyPolicy.currency().equals(currency)) {
            throw new PaymentJpaAdapterException("Currencies do not match");
        }
    }

    private void validateAmountDtoList(List<AmountDto> amountDtoList, Long invoiceId) {

        if (amountDtoList == null) {
            throw new PaymentJpaAdapterException("Amount DTO list is null");
        }

        if (amountDtoList.size() > 1) {
            throw new PaymentJpaAdapterException("Multiple currencies in invoice " + invoiceId);
        }
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new PaymentJpaAdapterException("Payment cannot be null");
        }
    }

    private void validateId(Long id, String fieldName) {

        IdValidation.validateId(
                id,
                fieldName,
                PaymentJpaAdapterException::new
        );
    }
}
