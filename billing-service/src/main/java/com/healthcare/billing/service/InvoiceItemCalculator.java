package com.healthcare.billing.service;

import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.value.Money;
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
            Long serviceId,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discountRate,
            BigDecimal taxRate
    ) {
        BillingValidation.validateId(serviceId);
        BillingValidation.validateDescription(description);
        BillingValidation.validateQuantity(quantity);
        BillingValidation.validateUnitPrice(unitPrice);
        BillingValidation.validateDiscountRate(discountRate);
        BillingValidation.validateTaxRate(taxRate);

        Money netAmount = moneyPolicy.moneyOf(quantity.multiply(unitPrice));

        Money discountAmount = moneyPolicy.moneyOf(netAmount.amount().multiply(discountRate));

        Money taxableAmount = netAmount.subtract(discountAmount);

        Money taxAmount = moneyPolicy.moneyOf(taxableAmount.amount().multiply(taxRate));

        Money totalAmount = taxableAmount.add(taxAmount);

        return InvoiceItem.builder()
                .serviceId(serviceId)
                .description(description.strip())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .discountRate(discountRate)
                .taxRate(taxRate)
                .netAmount(netAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .build();
    }
}