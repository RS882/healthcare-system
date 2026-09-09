package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.exception.InvoicePersistenceMappingException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Component
public class InvoiceReconstitutor {

    public Invoice restore(
            Long id,
            String invoiceNumber,
            Long patientId,
            Long medicalFacilityId,
            List<InvoiceItem> items,
            Money netAmount,
            Money discountAmount,
            Money taxAmount,
            Money totalAmount,
            InvoiceStatus status,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        validatePersistenceData(
                id,
                invoiceNumber,
                status,
                issuedDate,
                dueDate
        );

        Invoice invoice = Invoice.builder()
                .patientId(patientId)
                .medicalFacilityId(medicalFacilityId)
                .items(items)
                .netAmount(netAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .build();

        invoice.applyPersistenceData(
                id,
                invoiceNumber,
                status,
                issuedDate,
                dueDate
        );

        return invoice;
    }

    private void validatePersistenceData(
            Long id,
            String invoiceNumber,
            InvoiceStatus status,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        if (id == null || id <= 0) {
            throw new InvoicePersistenceMappingException("Persisted invoice id must be greater than zero");
        }

        if (status == null) {
            throw new InvoicePersistenceMappingException("Persisted invoice status must not be null");
        }

        switch (status) {
            case DRAFT -> validateDraft(
                    invoiceNumber,
                    issuedDate,
                    dueDate
            );

            case ISSUED, PAID -> validateIssuedState(
                    invoiceNumber,
                    issuedDate,
                    dueDate
            );

            case CANCELLED -> validateCancelledState(
                    invoiceNumber,
                    issuedDate,
                    dueDate
            );
        }
    }

    private void validateDraft(
            String invoiceNumber,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        if (StringUtils.hasText(invoiceNumber)) {
            throw new InvoicePersistenceMappingException("Draft invoice must not have an invoice number");
        }

        if (issuedDate != null) {
            throw new InvoicePersistenceMappingException("Draft invoice must not have an issued date");
        }

        if (dueDate != null) {
            throw new InvoicePersistenceMappingException("Draft invoice must not have a due date");
        }
    }

    private void validateIssuedState(
            String invoiceNumber,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        if (!StringUtils.hasText(invoiceNumber)) {
            throw new InvoicePersistenceMappingException("Issued invoice must have an invoice number");
        }

        if (issuedDate == null) {
            throw new InvoicePersistenceMappingException("Issued invoice must have an issued date");
        }

        if (dueDate == null) {
            throw new InvoicePersistenceMappingException("Issued invoice must have a due date");
        }

        if (dueDate.isBefore(issuedDate)) {
            throw new InvoicePersistenceMappingException("Persisted invoice due date must not be before issued date");
        }
    }

    private void validateCancelledState(
            String invoiceNumber,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        boolean hasInvoiceNumber = StringUtils.hasText(invoiceNumber);

        boolean hasIssuedDate = issuedDate != null;

        boolean hasDueDate = dueDate != null;

        boolean cancelledFromDraft =
                !hasInvoiceNumber
                        && !hasIssuedDate
                        && !hasDueDate;

        boolean cancelledFromIssued =
                hasInvoiceNumber
                        && hasIssuedDate
                        && hasDueDate;

        if (!cancelledFromDraft && !cancelledFromIssued) {
            throw new InvoicePersistenceMappingException("Cancelled invoice contains inconsistent issue data");
        }

        if (cancelledFromIssued && dueDate.isBefore(issuedDate)) {
            throw new InvoicePersistenceMappingException("Persisted invoice due date must not be before issued date");
        }
    }
}