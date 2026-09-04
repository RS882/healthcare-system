package com.healthcare.billing.model.entity;

import com.healthcare.billing.model.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    private static final String INVOICE_NUMBER = "INV-2026-000001";

    private static final LocalDate ISSUED_DATE = LocalDate.of(2026, 9, 4);

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 11);

    @Test
    void shouldIssueDraftInvoice() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        invoice.issue(
                INVOICE_NUMBER,
                ISSUED_DATE,
                DUE_DATE
        );

        assertAll(
                () -> assertEquals(
                        InvoiceStatus.ISSUED,
                        invoice.getStatus()
                ),
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
    void shouldCancelDraftInvoice() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        invoice.cancel();

        assertEquals(
                InvoiceStatus.CANCELLED,
                invoice.getStatus()
        );
    }

    @Test
    void shouldCancelIssuedInvoice() {
        Invoice invoice = createIssuedInvoice();

        invoice.cancel();

        assertEquals(
                InvoiceStatus.CANCELLED,
                invoice.getStatus()
        );
    }

    @Test
    void shouldMarkIssuedInvoiceAsPaid() {
        Invoice invoice = createIssuedInvoice();

        invoice.markAsPaid();

        assertEquals(
                InvoiceStatus.PAID,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectIssueFromIssuedState() {
        Invoice invoice = createIssuedInvoice();

        assertThrows(
                IllegalStateException.class,
                () -> invoice.issue(
                        "INV-2026-000002",
                        ISSUED_DATE,
                        DUE_DATE
                )
        );

        assertEquals(
                InvoiceStatus.ISSUED,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectIssueFromPaidState() {
        Invoice invoice = createIssuedInvoice();

        invoice.markAsPaid();

        assertThrows(
                IllegalStateException.class,
                () -> invoice.issue(
                        "INV-2026-000002",
                        ISSUED_DATE,
                        DUE_DATE
                )
        );

        assertEquals(
                InvoiceStatus.PAID,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectIssueFromCancelledState() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        invoice.cancel();

        assertThrows(
                IllegalStateException.class,
                () -> invoice.issue(
                        INVOICE_NUMBER,
                        ISSUED_DATE,
                        DUE_DATE
                )
        );

        assertEquals(
                InvoiceStatus.CANCELLED,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectPaymentOfDraftInvoice() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        assertThrows(
                IllegalStateException.class,
                invoice::markAsPaid
        );

        assertEquals(
                InvoiceStatus.DRAFT,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectPaymentOfCancelledInvoice() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        invoice.cancel();

        assertThrows(
                IllegalStateException.class,
                invoice::markAsPaid
        );

        assertEquals(
                InvoiceStatus.CANCELLED,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectPaymentOfPaidInvoice() {
        Invoice invoice = createIssuedInvoice();

        invoice.markAsPaid();

        assertThrows(
                IllegalStateException.class,
                invoice::markAsPaid
        );

        assertEquals(
                InvoiceStatus.PAID,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectCancellationOfPaidInvoice() {
        Invoice invoice = createIssuedInvoice();

        invoice.markAsPaid();

        assertThrows(
                IllegalStateException.class,
                invoice::cancel
        );

        assertEquals(
                InvoiceStatus.PAID,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectCancellationOfCancelledInvoice() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        invoice.cancel();

        assertThrows(
                IllegalStateException.class,
                invoice::cancel
        );

        assertEquals(
                InvoiceStatus.CANCELLED,
                invoice.getStatus()
        );
    }

    private Invoice createInvoice(
            InvoiceStatus status
    ) {
        return Invoice.builder()
                .status(status)
                .build();
    }

    private Invoice createIssuedInvoice() {
        Invoice invoice = createInvoice(
                InvoiceStatus.DRAFT
        );

        invoice.issue(
                INVOICE_NUMBER,
                ISSUED_DATE,
                DUE_DATE
        );

        return invoice;
    }
}