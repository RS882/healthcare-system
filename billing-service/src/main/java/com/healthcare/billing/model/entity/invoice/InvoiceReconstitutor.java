package com.healthcare.billing.model.entity.invoice;


import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import org.springframework.stereotype.Component;

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
}
