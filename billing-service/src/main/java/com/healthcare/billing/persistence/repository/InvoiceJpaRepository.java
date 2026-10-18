package com.healthcare.billing.persistence.repository;

import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, Long> {

    Optional<InvoiceJpaEntity> findByInvoiceNumber(String invoiceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.id = :id")
    Optional<InvoiceJpaEntity> findByIdWithLock(@Param("id") Long id);
}