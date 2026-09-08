package com.healthcare.billing.dto.invoice;

import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;


@Builder
public record InvoiceResponse(

        Long id,

        String invoiceNumber,

        Long patientId,

        Long medicalFacilityId,

        List<InvoiceItemResponse> items,

        Currency currency,

        BigDecimal netAmount,

        BigDecimal discountAmount,

        BigDecimal taxAmount,

        BigDecimal totalAmount,

        InvoiceStatus status,

        LocalDate issuedDate,

        LocalDate dueDate
) {
}