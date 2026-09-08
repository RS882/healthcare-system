package com.healthcare.billing.persistence.repository;

import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, Long> {

    Optional<InvoiceJpaEntity> findByInvoiceNumber(
            String invoiceNumber
    );
}