package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceTest {

    private static final String INVOICE_NUMBER = "INV-2026-000001";

    private static final LocalDate ISSUED_DATE = LocalDate.of(2026, 9, 4);

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 11);

    @Test
    void shouldApplyStatus() {
        Invoice invoice = createInvoice();

        invoice.applyStatus(InvoiceStatus.ISSUED);

        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }

    @Test
    void shouldApplyIssueData() {
        Invoice invoice = createInvoice();

        invoice.applyIssueData(INVOICE_NUMBER, ISSUED_DATE, DUE_DATE);

        assertAll(
                () -> assertEquals(
                        INVOICE_NUMBER,
                        invoice.getInvoiceNumber()
                ),
                () -> assertEquals(
                        ISSUED_DATE,
                        invoice.getIssuedDate()
                ),
                () -> assertEquals(
                        DUE_DATE,
                        invoice.getDueDate()
                )
        );
    }

    @Test
    void shouldStripInvoiceNumber() {
        Invoice invoice =  createInvoice();

        invoice.applyIssueData(
                "  INV-2026-000001  ",
                ISSUED_DATE,
                DUE_DATE
        );

        assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber());
    }

    private Invoice createInvoice() {
        return Invoice.builder()
                .build();
    }
}