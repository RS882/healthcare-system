package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.payment.persistence.entity.CashPaymentDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCashPaymentDetailsRepository
        extends JpaRepository<CashPaymentDetailsEntity, Long> {
}
