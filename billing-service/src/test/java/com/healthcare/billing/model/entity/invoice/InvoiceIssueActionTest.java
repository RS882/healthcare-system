package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.exception.InvoiceNumberGenerationException;
import com.healthcare.billing.generator.InvoiceNumberGenerator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceIssueActionTest {

    private static final String INVOICE_NUMBER = "INV-2026-000001";
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @Test
    void shouldApplyIssueData() {
        InvoiceIssueAction action = createAction(() -> INVOICE_NUMBER, 7);
        Invoice invoice = Invoice.builder().build();

        action.execute(invoice);

        assertAll(
                () -> assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber()),
                () -> assertEquals(LocalDate.of(2026, 9, 4), invoice.getIssuedDate()),
                () -> assertEquals(LocalDate.of(2026, 9, 11), invoice.getDueDate())
        );
    }

    @Test
    void shouldCalculateDueDateUsingPaymentTermDays() {
        InvoiceIssueAction action = createAction(() -> INVOICE_NUMBER, 14);
        Invoice invoice = Invoice.builder().build();

        action.execute(invoice);

        assertEquals(LocalDate.of(2026, 9, 18), invoice.getDueDate());
    }

    @Test
    void shouldStripGeneratedInvoiceNumber() {
        InvoiceIssueAction action = createAction(() -> "  " + INVOICE_NUMBER + "  ", 7);
        Invoice invoice = Invoice.builder().build();

        action.execute(invoice);

        assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber());
    }

    @Test
    void shouldRejectNullGeneratedInvoiceNumber() {
        InvoiceIssueAction action = createAction(() -> null, 7);
        Invoice invoice = Invoice.builder().build();

        assertThrows(
                InvoiceNumberGenerationException.class,
                () -> action.execute(invoice)
        );

        assertNull(invoice.getInvoiceNumber());
        assertNull(invoice.getIssuedDate());
        assertNull(invoice.getDueDate());
    }

    @Test
    void shouldRejectBlankGeneratedInvoiceNumber() {
        InvoiceIssueAction action = createAction(() -> "   ", 7);

        assertThrows(
                InvoiceNumberGenerationException.class,
                () -> action.execute(Invoice.builder().build())
        );
    }

    private InvoiceIssueAction createAction(
            InvoiceNumberGenerator generator,
            int paymentTermDays
    ) {
        return new InvoiceIssueAction(
                generator,
                new BillingProperties("EUR", paymentTermDays),
                FIXED_CLOCK
        );
    }
}
