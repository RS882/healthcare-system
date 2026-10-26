package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.PaymentBalanceValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.validation.MoneyValidation;
import lombok.Builder;
import lombok.Getter;

@Builder
public record PaymentBalance(
        Money remainingAmount,
        Money overpaidAmount
) {
    public PaymentBalance {

        validateAmount(remainingAmount, AmountType.REMAINING_AMOUNT);
        validateAmount(overpaidAmount, AmountType.OVERPAID_AMOUNT);

        validateAmounts(remainingAmount, overpaidAmount);
    }

    private static void validateAmounts(Money remainingAmount, Money overpaidAmount) {

        if (!remainingAmount.currency().equals(overpaidAmount.currency())) {
            throw new PaymentBalanceValidationException("Remaining currency must be equal to overpaid currency for payment balance");
        }

        if (remainingAmount.isPositive() && overpaidAmount.isPositive()) {
            throw new PaymentBalanceValidationException("Both sums for payment balance are positive.");
        }
    }

    private static void validateAmount(Money amount, AmountType amountType) {

        MoneyValidation.validateNonNegative(
                amount,
                amountType.getType(),
                PaymentBalanceValidationException::new);
    }

    @Getter
    private enum AmountType {
        REMAINING_AMOUNT("Remaining amount"),
        OVERPAID_AMOUNT("Overpaid amount");

        private final String type;

        AmountType(String type) {
            this.type = type;
        }
    }

}
