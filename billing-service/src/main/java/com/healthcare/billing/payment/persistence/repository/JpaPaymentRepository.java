package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.persistence.dto.AmountDto;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface JpaPaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByInvoiceId(Long invoiceId);

    @Query("""
            SELECT new com.healthcare.billing.payment.persistence.dto.AmountDto(
                        SUM(p.amount), p.currency)
            FROM PaymentEntity p           
            WHERE p.status = :status
              AND p.invoiceId = :invoiceId
            GROUP BY p.currency      
            """)
    List<AmountDto> calculatePaidAmountPerCurrency(
            @Param("invoiceId") Long invoiceId,
            @Param("status") PaymentStatus status);
}
