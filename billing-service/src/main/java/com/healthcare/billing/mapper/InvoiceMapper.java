package com.healthcare.billing.mapper;

import com.healthcare.billing.dto.invoice.InvoiceItemResponse;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.value.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {

        if (invoice == null) {
            return null;
        }

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .patientId(invoice.getPatientId())
                .medicalFacilityId(invoice.getMedicalFacilityId())
                .items(toItemResponses(invoice.getItems()))
                .currency(resolveCurrency(invoice))
                .netAmount(toAmount(invoice.getNetAmount()))
                .discountAmount(toAmount(invoice.getDiscountAmount()))
                .taxAmount(toAmount(invoice.getTaxAmount()))
                .totalAmount(toAmount(invoice.getTotalAmount()))
                .status(invoice.getStatus())
                .issuedDate(invoice.getIssuedDate())
                .dueDate(invoice.getDueDate())
                .build();
    }

    private List<InvoiceItemResponse> toItemResponses(List<InvoiceItem> items) {

        if (items == null) {
            return null;
        }

        return items.stream()
                .map(this::toItemResponse)
                .toList();
    }

    private InvoiceItemResponse toItemResponse(
            InvoiceItem item
    ) {

        return InvoiceItemResponse.builder()
                .id(item.getId())
                .serviceId(item.getServiceId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountRate(item.getDiscountRate())
                .taxRate(item.getTaxRate())
                .netAmount(toAmount(item.getNetAmount()))
                .discountAmount(toAmount(item.getDiscountAmount()))
                .taxAmount(toAmount(item.getTaxAmount()))
                .totalAmount(toAmount(item.getTotalAmount()))
                .build();
    }

    private BigDecimal toAmount(Money money) {

        if (money == null) {
            return null;
        }

        return money.amount();
    }

    private Currency resolveCurrency(Invoice invoice) {

        if (invoice.getTotalAmount() == null) {
            return null;
        }

        return invoice.getTotalAmount().currency();
    }
}
