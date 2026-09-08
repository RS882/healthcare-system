package com.healthcare.billing.model.service;

import java.math.BigDecimal;
import java.util.Currency;

public record MedicalServiceData(
        Long id,
        String description,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        Currency currency
) {
}
