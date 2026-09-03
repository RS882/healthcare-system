package com.healthcare.billing.service;

import com.healthcare.billing.model.entity.Invoice;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class InvoiceCalculator {

    private final MoneyPolicy moneyPolicy;

    public Invoice calculate(
            Long patientId,
            Long medicalFacilityId,
            List<InvoiceItem> items
    ) {
        validateItems(items);

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
                .status(InvoiceStatus.DRAFT)
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

    private void validateItems(List<InvoiceItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invoice items must not be empty"
            );
        }

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Invoice items must not contain null"
            );
        }
    }
}