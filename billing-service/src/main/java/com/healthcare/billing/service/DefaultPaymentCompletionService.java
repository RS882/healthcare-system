package com.healthcare.billing.service;

import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.PaymentCompletionResult;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.service.interfaces.PaymentCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultPaymentCompletionService implements PaymentCompletionService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentCompletionResult<? extends PaymentMethodDetails> completePayment(PaymentCompletionCommand command) {
        return null;
    }
}
