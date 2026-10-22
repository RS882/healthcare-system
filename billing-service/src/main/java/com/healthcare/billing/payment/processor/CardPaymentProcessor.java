package com.healthcare.billing.payment.processor;

import com.healthcare.billing.exception.CardPaymentVerificationResultException;
import com.healthcare.billing.payment.command.CardPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.card_payment.CardPaymentGateway;
import com.healthcare.billing.payment.payment_method_details.card_payment.CardPaymentVerificationResult;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentVerificationStatus;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.payment.validation.CardPaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
public class CardPaymentProcessor
        implements PaymentProcessor<CardPaymentCompletionCommand, CardPaymentMethodDetails> {

    private final CardPaymentGateway paymentGateway;
    private final CardPaymentValidator validator;
    private final PaymentStateResolver resolver;
    private final Clock clock;

    @Override
    public CardPaymentMethodDetails process(
            Payment payment,
            CardPaymentCompletionCommand command
    ) {

        validator.validate(payment, command);

        CardPaymentVerificationResult result = paymentGateway.verifyTransaction(
                command.providerTransactionId(),
                command.provider()
        );

        validatePaymentVerificationResult(result, payment, command);

        validateCardPaymentVerificationStatus(result.status());

        CardPaymentMethodDetails details = new CardPaymentMethodDetails(
                result.providerTransactionId(),
                result.provider()
        );

        payment.start(clock.instant(), resolver);

        payment.complete(clock.instant(), resolver);

        return details;
    }

    @Override
    public Class<CardPaymentCompletionCommand> supportedCommandType() {
        return CardPaymentCompletionCommand.class;
    }

    private void validateCardPaymentVerificationStatus(
            CardPaymentVerificationStatus status) {

        if (!CardPaymentVerificationStatus.SUCCEEDED.equals(status)) {
            throw new CardPaymentVerificationResultException("Card payment verification status is %s"
                    .formatted(status.name()));
        }
    }

    private void validatePaymentVerificationResult(
            CardPaymentVerificationResult result,
            Payment payment,
            CardPaymentCompletionCommand command) {

        if (result == null) {
            throw new CardPaymentVerificationResultException("Card payment verification result is null");
        }

        if (!payment.getAmount().equals(result.amount())) {
            throw new CardPaymentVerificationResultException("Payment amount must be equals verification amount ");
        }

        if (!result.providerTransactionId().equals(command.providerTransactionId())) {
            throw new CardPaymentVerificationResultException("Payment provider transaction id mismatch");
        }
        if (result.provider() != command.provider()) {
            throw new CardPaymentVerificationResultException("Payment provider mismatch");
        }
    }
}
