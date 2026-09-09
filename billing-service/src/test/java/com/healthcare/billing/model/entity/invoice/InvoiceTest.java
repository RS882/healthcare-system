package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    private static final String INVOICE_NUMBER = "INV-2026-000001";
    private static final LocalDate ISSUED_DATE = LocalDate.of(2026, 9, 4);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 11);

    @Test
    void shouldCreateInvoiceAsDraft() {
        assertEquals(InvoiceStatus.DRAFT, createInvoice().getStatus());
    }

    @Test
    void shouldApplyStatus() {
        Invoice invoice = createInvoice();

        invoice.applyStatus(InvoiceStatus.ISSUED);

        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                InvoiceValidationException.class,
                () -> createInvoice().applyStatus(null)
        );
    }

    @Test
    void shouldApplyIssueData() {
        Invoice invoice = createInvoice();

        invoice.applyIssueData(INVOICE_NUMBER, ISSUED_DATE, DUE_DATE);

        assertAll(
                () -> assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber()),
                () -> assertEquals(ISSUED_DATE, invoice.getIssuedDate()),
                () -> assertEquals(DUE_DATE, invoice.getDueDate())
        );
    }

    @Test
    void shouldStripInvoiceNumber() {
        Invoice invoice = createInvoice();

        invoice.applyIssueData("  INV-2026-000001  ", ISSUED_DATE, DUE_DATE);

        assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber());
    }

    @Test
    void shouldRejectNullInvoiceNumber() {
        assertThrows(
                InvoiceValidationException.class,
                () -> createInvoice().applyIssueData(null, ISSUED_DATE, DUE_DATE)
        );
    }

    @Test
    void shouldRejectBlankInvoiceNumber() {
        assertThrows(
                InvoiceValidationException.class,
                () -> createInvoice().applyIssueData("   ", ISSUED_DATE, DUE_DATE)
        );
    }

    @Test
    void shouldRejectNullIssuedDate() {
        assertThrows(
                InvoiceValidationException.class,
                () -> createInvoice().applyIssueData(INVOICE_NUMBER, null, DUE_DATE)
        );
    }

    @Test
    void shouldRejectNullDueDate() {
        assertThrows(
                InvoiceValidationException.class,
                () -> createInvoice().applyIssueData(INVOICE_NUMBER, ISSUED_DATE, null)
        );
    }

    @Test
    void shouldRejectDueDateBeforeIssuedDate() {
        assertThrows(
                InvoiceValidationException.class,
                () -> createInvoice().applyIssueData(
                        INVOICE_NUMBER,
                        ISSUED_DATE,
                        ISSUED_DATE.minusDays(1)
                )
        );
    }

    @Test
    void shouldAllowDueDateEqualToIssuedDate() {
        assertDoesNotThrow(
                () -> createInvoice().applyIssueData(
                        INVOICE_NUMBER,
                        ISSUED_DATE,
                        ISSUED_DATE
                )
        );
    }

    private Invoice createInvoice() {
        return Invoice.builder().build();
    }
}
