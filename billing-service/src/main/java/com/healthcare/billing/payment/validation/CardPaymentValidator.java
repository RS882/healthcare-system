package com.healthcare.billing.payment.validation;

import com.healthcare.billing.payment.command.CardPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentValidator
        extends AbstractPaymentValidator<CardPaymentCompletionCommand> {

    @Override
    public void validate(Payment payment, CardPaymentCompletionCommand command) {

        validateCommand(command);

        validatePayment(payment, PaymentMethod.CARD);

        validateIdForPaymentAndCommand(payment.getId(), command.paymentId());

    }
}
