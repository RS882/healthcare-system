package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.generator.InvoiceNumberGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceIssueActionTest {

    private static final String INVOICE_NUMBER = "INV-2026-000001";

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @Test
    void shouldApplyIssueData() {

        InvoiceNumberGenerator generator = () -> INVOICE_NUMBER;

        BillingProperties properties = new BillingProperties("EUR", 7);

        InvoiceIssueAction action = new InvoiceIssueAction(generator, properties, FIXED_CLOCK);

        Invoice invoice = Invoice.builder().build();

        action.execute(invoice);

        assertAll(
                () -> assertEquals(
                        INVOICE_NUMBER,
                        invoice.getInvoiceNumber()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 9, 4),
                        invoice.getIssuedDate()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 9, 11),
                        invoice.getDueDate()
                )
        );
    }

    @Test
    void shouldCalculateDueDateUsingPaymentTermDays() {

        InvoiceNumberGenerator generator = () -> INVOICE_NUMBER;

        BillingProperties properties = new BillingProperties("EUR", 14);

        InvoiceIssueAction action = new InvoiceIssueAction(
                generator,
                properties,
                FIXED_CLOCK
        );

        Invoice invoice = Invoice.builder().build();

        action.execute(invoice);

        assertEquals(
                LocalDate.of(2026, 9, 18),
                invoice.getDueDate()
        );
    }
}