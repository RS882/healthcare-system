package com.healthcare.billing.payment.model;

import com.healthcare.billing.model.value.Money;
import lombok.Builder;

@Builder
public record PaymentBalance(
        Money remainingAmount,
        Money overpaidAmount
) {
}
