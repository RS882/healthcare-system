package com.healthcare.billing.payment.persistence.dto;

import java.math.BigDecimal;


public record AmountDto(
        BigDecimal amount,
        String currency
) {
}
