package com.healthcare.billing.service;

import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import com.healthcare.billing.validation.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class InvoiceCalculator {

    private final MoneyPolicy moneyPolicy;
    private final InvoiceValidator validator;

    public Invoice calculate(
            Long patientId,
            Long medicalFacilityId,
            List<InvoiceItem> items
    ) {
        validator.validateItems(items);

        Money netAmount = sum(items, InvoiceItem::getNetAmount);

        Money discountAmount = sum(items, InvoiceItem::getDiscountAmount);

        Money taxAmount = sum(items, InvoiceItem::getTaxAmount);

        Money totalAmount = sum(items, InvoiceItem::getTotalAmount);

        return Invoice.builder()
                .patientId(patientId)
                .medicalFacilityId(medicalFacilityId)
                .items(List.copyOf(items))
                .netAmount(netAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .build();
    }

    private Money sum(
            List<InvoiceItem> items,
            Function<InvoiceItem, Money> extractor
    ) {
        return items.stream()
                .map(extractor)
                .reduce(
                        moneyPolicy.zero(),
                        Money::add
                );
    }
}