package com.healthcare.billing.dto.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateInvoiceRequest(

        @NotNull
        @Positive
        Long patientId,

        @NotNull
        @Positive
        Long medicalFacilityId,

        @NotEmpty
        List<@NotNull @Valid CreateInvoiceItemRequest> items
) {
}
