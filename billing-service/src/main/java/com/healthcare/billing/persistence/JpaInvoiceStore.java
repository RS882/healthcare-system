package com.healthcare.billing.persistence;

import com.healthcare.billing.config.propertie.BillingInvoiceTimeoutProperties;
import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import com.healthcare.billing.persistence.mapper.InvoicePersistenceMapper;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JpaInvoiceStore implements InvoiceStore {

    private final InvoiceJpaRepository invoiceJpaRepository;
    private final InvoicePersistenceMapper invoicePersistenceMapper;
    private final JdbcTemplate jdbcTemplate;
    private final BillingInvoiceTimeoutProperties billingInvoiceTimeoutProperties;

    @Override
    @Transactional(readOnly = true)
    public Invoice findById(Long id) {

        validateId(id);

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        return invoicePersistenceMapper.toDomain(entity);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Invoice findByIdWithBlock(Long id) {

        validateId(id);

        jdbcTemplate.queryForObject(
                "SELECT set_config('lock_timeout', ?, true)",
                String.class,
                billingInvoiceTimeoutProperties.lockTimeoutMs() + "ms"
        );

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findByIdWithLock(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        return invoicePersistenceMapper.toDomain(entity);
    }

    @Override
    public Invoice findByInvoiceNumber(String invoiceNumber) {

        validateInvoiceNumber(invoiceNumber);

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findByInvoiceNumber(invoiceNumber.strip())
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceNumber));

        return invoicePersistenceMapper.toDomain(entity);
    }

    @Override
    public Invoice save(Invoice invoice) {

        validateInvoice(invoice);

        InvoiceJpaEntity entity = invoicePersistenceMapper.toEntity(invoice);

        InvoiceJpaEntity savedEntity = invoiceJpaRepository.save(entity);

        return invoicePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void update(Invoice invoice) {

        validateInvoice(invoice);

        Long id = invoice.getId();

        validateId(id);

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        invoicePersistenceMapper.updateEntity(invoice, entity);
    }

    @Override
    public boolean existsById(Long id) {

        validateId(id);

        return invoiceJpaRepository.existsById(id);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvoiceValidationException("Invoice id cannot be null");
        }
        if (id <= 0) {
            throw new InvoiceValidationException("Invoice id must be greater than zero");
        }
    }

    private void validateInvoiceNumber(String invoiceNumber) {

        if (!StringUtils.hasText(invoiceNumber)) {
            throw new InvoiceValidationException("Invoice number must not be null or blank");
        }
    }

    private void validateInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new InvoiceValidationException("Invoice cannot be null");
        }
    }
}
