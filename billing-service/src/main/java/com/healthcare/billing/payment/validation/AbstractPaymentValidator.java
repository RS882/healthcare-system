package com.healthcare.billing.payment.validation;

import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.validation.IdValidation;

import java.util.Objects;

public abstract class AbstractPaymentValidator<T extends PaymentCompletionCommand> implements PaymentValidator<T> {

    protected static void validateIdForPaymentAndCommand(Long paymentId, Long commandPaymentId) {

        IdValidation.validateId(paymentId, "Payment id", PaymentValidationException::new);

        IdValidation.validateId(commandPaymentId, "Payment id", PaymentValidationException::new);

        if (!Objects.equals(paymentId, commandPaymentId)) {
            throw new PaymentValidationException("Payment id mismatch");
        }
    }

    protected static void validateCommand(PaymentCompletionCommand command) {

        if (command == null) {
            throw new PaymentValidationException("Payment completion command is null");
        }
    }

    protected static void validatePayment(Payment payment, PaymentMethod paymentMethod) {

        if (payment == null) {
            throw new PaymentValidationException("Payment cannot be null");
        }

        if (paymentMethod == null) {
            throw new PaymentValidationException("Payment method cannot be null");
        }

        if (payment.getMethod() != paymentMethod) {
            throw new PaymentValidationException(
                    "Payment method must be <%s>".formatted(paymentMethod.name()));
        }
    }
}
