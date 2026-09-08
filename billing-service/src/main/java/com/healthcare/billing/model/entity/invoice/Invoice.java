package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
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

    @Builder
    private Invoice(
            Long patientId,
            Long medicalFacilityId,
            List<InvoiceItem> items,
            Money netAmount,
            Money discountAmount,
            Money taxAmount,
            Money totalAmount
    ) {
        this.patientId = patientId;
        this.medicalFacilityId = medicalFacilityId;
        this.items = items;
        this.netAmount = netAmount;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;

        this.status = InvoiceStatus.DRAFT;
    }

    void applyStatus(InvoiceStatus status) {
        this.status = status;
    }

    void applyIssueData(
            String invoiceNumber,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        this.invoiceNumber = invoiceNumber.strip();
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
    }

    void applyPersistenceData(
            Long id,
            String invoiceNumber,
            InvoiceStatus status,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.status = status;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
    }
}