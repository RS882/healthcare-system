package com.healthcare.billing.persistence;

import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import com.healthcare.billing.persistence.mapper.InvoicePersistenceMapper;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaInvoiceStore implements InvoiceStore {

    private final InvoiceJpaRepository invoiceJpaRepository;
    private final InvoicePersistenceMapper invoicePersistenceMapper;

    @Override
    public Invoice findById(Long id) {

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        return invoicePersistenceMapper.toDomain(entity);
    }

    @Override
    public Invoice findByInvoiceNumber(String invoiceNumber) {

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceNumber));

        return invoicePersistenceMapper.toDomain(entity);
    }

    @Override
    public Invoice save(Invoice invoice) {

        InvoiceJpaEntity entity = invoicePersistenceMapper.toEntity(invoice);

        InvoiceJpaEntity savedEntity = invoiceJpaRepository.save(entity);

        return invoicePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void update(Invoice invoice) {

        InvoiceJpaEntity entity = invoiceJpaRepository
                .findById(invoice.getId())
                .orElseThrow(() -> new InvoiceNotFoundException(invoice.getId()));

        invoicePersistenceMapper.updateEntity(invoice, entity);
    }
}
