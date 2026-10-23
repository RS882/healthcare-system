package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.payment.persistence.entity.CardPaymentDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCardPaymentDetailsRepository
        extends JpaRepository<CardPaymentDetailsEntity, Long> {

}
