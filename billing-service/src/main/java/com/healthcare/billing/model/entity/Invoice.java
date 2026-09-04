package com.healthcare.billing.model.entity;

import com.healthcare.billing.model.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class Invoice {

    private Long id;

    private String invoiceNumber;

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

    public void issue(
            String invoiceNumber,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {

        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft invoice can be issued"
            );
        }
        this.invoiceNumber = invoiceNumber.strip();
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.ISSUED;
    }

    public void cancel() {

        if (status != InvoiceStatus.DRAFT
                && status != InvoiceStatus.ISSUED) {
            throw new IllegalStateException(
                    "Only draft or issued invoice can be cancelled"
            );
        }

        this.status = InvoiceStatus.CANCELLED;
    }

    public void markAsPaid() {

        if (status != InvoiceStatus.ISSUED) {
            throw new IllegalStateException(
                    "Only issued invoice can be paid"
            );
        }

        this.status = InvoiceStatus.PAID;
    }
}