package com.healthcare.billing.payment.processor;

import com.healthcare.billing.exception.CashChangeCalculatorException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CashPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.cash_payment.CashChangeCalculator;
import com.healthcare.billing.payment.payment_method_details.cash_payment.ChangeBreakdown;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.payment.validation.CashPaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
public class CashPaymentProcessor
        implements PaymentProcessor<CashPaymentCompletionCommand, CashPaymentMethodDetails> {

    private final CashChangeCalculator calculator;
    private final PaymentStateResolver resolver;
    private final Clock clock;

    @Override
    public CashPaymentMethodDetails process(
            Payment payment,
            CashPaymentCompletionCommand command
    ) {

        CashPaymentValidator.validate(payment, command);

        payment.start(clock.instant(), resolver);

        try {

            Money receivedAmount = command.receivedAmount();

            ChangeBreakdown breakdown = calculator.calculate(payment.getAmount(), receivedAmount);

            CashPaymentMethodDetails details = new CashPaymentMethodDetails(
                    receivedAmount,
                    breakdown);

            payment.complete(clock.instant(), resolver);

            return details;

        } catch (CashChangeCalculatorException |
                 UnsupportedCurrencyException e) {

            payment.cancel(clock.instant(), resolver);

            throw e;
        }
    }
}