package com.healthcare.billing.dto.invoice;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InvoiceItemResponse(

        Long id,

        Long serviceId,

        String description,

        BigDecimal quantity,

        BigDecimal unitPrice,

        BigDecimal discountRate,

        BigDecimal taxRate,

        BigDecimal netAmount,

        BigDecimal discountAmount,

        BigDecimal taxAmount,

        BigDecimal totalAmount
) {
}
