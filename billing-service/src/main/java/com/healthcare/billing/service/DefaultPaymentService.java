package com.healthcare.billing.service;

import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.exception.PaymentNotFoundException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.persistence.InvoiceStore;
import com.healthcare.billing.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceStore invoiceStore;
    private final Clock clock;

    @Override
    public Payment retrievePaymentById(Long id) {

        validateId(id, "Payment ID");

        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    public List<Payment> retrievePaymentsByInvoiceId(Long invoiceId) {

        validateId(invoiceId, "Invoice ID");

        return paymentRepository.findByInvoiceId(invoiceId);
    }

    @Override
    public Payment createPayment(
            Long invoiceId,
            Money amount,
            PaymentMethod paymentMethod
    ) {

        validateId(invoiceId, "Invoice ID");

        validateInvoiceExists(invoiceId);

        Payment newPayment = Payment.builder()
                .invoiceId(invoiceId)
                .amount(amount)
                .method(paymentMethod)
                .createdAt(clock.instant())
                .build();

        return paymentRepository.save(newPayment);
    }

    private void validateInvoiceExists(Long invoiceId) {
        if (!invoiceStore.existsById(invoiceId)) {
            throw new InvoiceNotFoundException(invoiceId);
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null) {
            throw new PaymentValidationException("%s cannot be null".formatted(fieldName));
        }
        if (id <= 0) {
            throw new PaymentValidationException("%s must be greater than zero".formatted(fieldName));
        }
    }
}
