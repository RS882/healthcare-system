package com.healthcare.billing.payment.processor;

import com.healthcare.billing.payment.command.CardPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentProcessor
        implements PaymentProcessor<CardPaymentCompletionCommand, CardPaymentMethodDetails> {


    @Override
    public CardPaymentMethodDetails process(
            Payment payment,
            CardPaymentCompletionCommand command
    ) {
        return null;
    }

    @Override
    public Class<CardPaymentCompletionCommand> supportedCommandType() {
        return CardPaymentCompletionCommand.class;
    }
}
