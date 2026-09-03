package com.healthcare.billing.service;

import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.money.MoneyPolicy;
import com.healthcare.billing.validation.BillingValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InvoiceItemCalculator {

    private final MoneyPolicy moneyPolicy;

    public InvoiceItem calculate(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discountRate,
            BigDecimal taxRate
    ) {
        BillingValidation.validateDescription(description);
        BillingValidation.validateQuantity(quantity);
        BillingValidation.validateUnitPrice(unitPrice);
        BillingValidation.validateDiscountRate(discountRate);
        BillingValidation.validateTaxRate(taxRate);

        BigDecimal rawNetAmount = quantity.multiply(unitPrice);

        BigDecimal rawDiscountAmount = rawNetAmount.multiply(discountRate);

        BigDecimal rawTaxableAmount = rawNetAmount.subtract(rawDiscountAmount);

        BigDecimal rawTaxAmount = rawTaxableAmount.multiply(taxRate);

        BigDecimal rawTotalAmount = rawTaxableAmount.add(rawTaxAmount);

        return InvoiceItem.builder()
                .description(description.strip())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .discountRate(discountRate)
                .taxRate(taxRate)
                .netAmount(moneyPolicy.moneyOf(rawNetAmount))
                .discountAmount(moneyPolicy.moneyOf(rawDiscountAmount))
                .taxAmount(moneyPolicy.moneyOf(rawTaxAmount))
                .totalAmount(moneyPolicy.moneyOf(rawTotalAmount))
                .build();
    }
}