package com.healthcare.billing.payment.resolver;

import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.payment.model.*;
import com.healthcare.billing.payment.model.enums.PaymentStatus;

public class PaymentStateResolver {

    private final PaymentState createdState = new CreatedPaymentState();
    private final PaymentState pendingState = new PendingPaymentState();
    private final PaymentState completedState = new CompletedPaymentState();
    private final PaymentState cancelledState = new CancelledPaymentState();

    public PaymentState resolve(PaymentStatus status) {

        validateStatus(status);

        return switch (status) {
            case CREATED -> createdState;
            case PENDING -> pendingState;
            case COMPLETED -> completedState;
            case CANCELLED -> cancelledState;
        };
    }

    private void validateStatus(PaymentStatus status) {
        if (status == null) {
            throw new PaymentValidationException("Payment status cannot be null");
        }
    }
}
