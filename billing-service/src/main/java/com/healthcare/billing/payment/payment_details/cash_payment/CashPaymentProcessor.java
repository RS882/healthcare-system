package com.healthcare.billing.payment.payment_details.cash_payment;

import com.healthcare.billing.exception.CashChangeCalculatorException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_details.CashPaymentDetails;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.CashChangeCalculator;
import com.healthcare.billing.payment.payment_details.cash_payment.cash_change_calculator.ChangeBreakdown;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.payment.validation.CashPaymentValidator;

import java.time.Clock;

public class CashPaymentProcessor {

    private final CashPaymentValidator validator;
    private final CashChangeCalculator calculator;
    private final PaymentStateResolver resolver;
    private final Clock clock;

    public CashPaymentProcessor(
            CashPaymentValidator validator,
            CashChangeCalculator calculator,
            PaymentStateResolver resolver,
            Clock clock) {

        this.validator = validator;
        this.calculator = calculator;
        this.resolver = resolver;
        this.clock = clock;
    }

    public CashPaymentDetails process(
            Payment payment,
            Money receivedAmount
    ) {

        validator.validate(payment, receivedAmount);

        payment.start(clock.instant(), resolver);

        try {

            ChangeBreakdown breakdown = calculator.calculate(payment.getAmount(), receivedAmount);

            payment.complete(clock.instant(), resolver);

            return new CashPaymentDetails(
                    payment,
                    receivedAmount,
                    breakdown);

        } catch (CashChangeCalculatorException |
                 UnsupportedCurrencyException e) {

            payment.cancel(clock.instant(), resolver);

            throw e;
        }
    }
}