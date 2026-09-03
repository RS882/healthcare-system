package com.healthcare.billing.model.entity;


import com.healthcare.billing.model.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Invoice {

    private Long id;

    private UUID invoiceNumber;

    private Long patientId;

    private Long medicalFacilityId;

    private List<InvoiceItem> items;

    private Money netAmount;

    private Money discountAmount;

    private Money taxAmount;

    private Money totalAmount;

    private InvoiceStatus status;

    private LocalDate issuedDate;

    private LocalDate dueDate;
}
