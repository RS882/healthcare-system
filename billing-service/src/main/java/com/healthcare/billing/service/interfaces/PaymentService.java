package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.payment.model.Payment;

public interface PaymentService {

    Payment retrievePaymentById(Long id);
}
