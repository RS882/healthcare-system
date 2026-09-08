package com.healthcare.billing.dto.invoice;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateInvoiceItemRequest(

        @NotNull
        @Positive
        Long serviceId,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotNull
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "1.0")
        BigDecimal discountRate
) {
}