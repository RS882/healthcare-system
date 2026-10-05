package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;

import java.util.List;

public interface PaymentService {

    Payment retrievePaymentById(Long id);

    List<Payment> retrievePaymentsByInvoiceId(Long invoiceId);

    Payment createPayment(Long invoiceId, Money amount, PaymentMethod paymentMethod);
}
