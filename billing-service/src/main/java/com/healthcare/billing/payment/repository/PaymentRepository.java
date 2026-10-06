package com.healthcare.billing.payment.repository;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findByInvoiceId(Long invoiceId);

    Money calculatePaidAmount(Long invoiceId);
}
