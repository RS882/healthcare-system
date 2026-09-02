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
            BigDecimal taxRate
    ) {
        BillingValidation.validateDescription(description);
        BillingValidation.validateQuantity(quantity);
        BillingValidation.validateUnitPrice(unitPrice);
        BillingValidation.validateTaxRate(taxRate);

        BigDecimal rawNetAmount = quantity.multiply(unitPrice);

        BigDecimal rawTaxAmount = rawNetAmount.multiply(taxRate);

        BigDecimal rawTotalAmount = rawNetAmount.add(rawTaxAmount);

        return InvoiceItem.builder()
                .description(description.strip())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .taxRate(taxRate)
                .netAmount(
                        moneyPolicy.moneyOf(rawNetAmount)
                )
                .taxAmount(
                        moneyPolicy.moneyOf(rawTaxAmount)
                )
                .totalAmount(
                        moneyPolicy.moneyOf(rawTotalAmount)
                )
                .build();
    }
}