package com.healthcare.billing.payment.validation;

import com.healthcare.billing.exception.CashPaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CashPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CashPaymentValidator
        extends AbstractPaymentValidator<CashPaymentCompletionCommand> {

    @Override
    public void validate(Payment payment, CashPaymentCompletionCommand command) {

        validateCommand(command);

        validatePayment(payment, PaymentMethod.CASH);

        validateIdForPaymentAndCommand(payment.getId(), command.paymentId());

        validateReceivedAmount(command.receivedAmount(), payment);
    }

    private void validateReceivedAmount(Money receivedAmount, Payment payment) {

        Currency receivedAmountCurrency = receivedAmount.currency();
        Currency paymentCurrency = payment.getAmount().currency();

        if (!receivedAmountCurrency.equals(paymentCurrency)) {
            throw new CashPaymentValidationException(
                    "Received amount currency <%s> does not match payment currency <%s>"
                            .formatted(
                                    receivedAmountCurrency.getCurrencyCode(),
                                    paymentCurrency.getCurrencyCode()
                            )
            );
        }

        Money paymentAmount = payment.getAmount();

        if (receivedAmount.compareTo(paymentAmount) < 0) {
            throw new CashPaymentValidationException(
                    "Received amount <%s> is less than payment amount <%s>"
                            .formatted(receivedAmount, paymentAmount)
            );
        }
    }
}
