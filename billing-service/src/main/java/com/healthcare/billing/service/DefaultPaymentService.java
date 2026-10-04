package com.healthcare.billing.service;

import com.healthcare.billing.exception.PaymentNotFoundException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment retrievePaymentById(Long id) {

        validateId(id);

        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new PaymentValidationException("Payment id is null");
        }

        if (id <= 0) {
            throw new PaymentValidationException("Payment id must be greater than zero");
        }
    }
}
