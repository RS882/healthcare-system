package com.healthcare.billing.service;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.generator.InvoiceNumberGenerator;
import com.healthcare.billing.model.entity.Invoice;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.validation.InvoiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class DefaultInvoiceServiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private static final String INVOICE_NUMBER = "INV-2026-000001";

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private DefaultInvoiceService invoiceService;

    @BeforeEach
    void setUp() {

        InvoiceNumberGenerator invoiceNumberGenerator =
                () -> INVOICE_NUMBER;

        BillingProperties billingProperties =
                new BillingProperties(
                        "EUR",
                        7
                );

        InvoiceValidator invoiceValidator =
                new InvoiceValidator();

        invoiceService =
                new DefaultInvoiceService(
                        invoiceNumberGenerator,
                        billingProperties,
                        invoiceValidator,
                        FIXED_CLOCK
                );
    }

    @Test
    void shouldIssueDraftInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        Invoice result =
                invoiceService.issue(invoice);

        assertSame(
                invoice,
                result
        );

        assertAll(
                () -> assertEquals(
                        InvoiceStatus.ISSUED,
                        result.getStatus()
                ),
                () -> assertEquals(
                        INVOICE_NUMBER,
                        result.getInvoiceNumber()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 9, 4),
                        result.getIssuedDate()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 9, 11),
                        result.getDueDate()
                )
        );
    }

    @Test
    void shouldRejectNullInvoice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.issue(null)
        );
    }

    @Test
    void shouldRejectAlreadyIssuedInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        invoiceService.issue(invoice);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.issue(invoice)
        );

        assertEquals(
                InvoiceStatus.ISSUED,
                invoice.getStatus()
        );
    }

    @Test
    void shouldCancelDraftInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        Invoice result =
                invoiceService.cancel(invoice);

        assertSame(
                invoice,
                result
        );

        assertEquals(
                InvoiceStatus.CANCELLED,
                result.getStatus()
        );
    }

    @Test
    void shouldCancelIssuedInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        invoiceService.issue(invoice);

        Invoice result =
                invoiceService.cancel(invoice);

        assertSame(
                invoice,
                result
        );

        assertEquals(
                InvoiceStatus.CANCELLED,
                result.getStatus()
        );
    }

    @Test
    void shouldRejectCancellationOfPaidInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        invoiceService.issue(invoice);
        invoiceService.markAsPaid(invoice);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.cancel(invoice)
        );

        assertEquals(
                InvoiceStatus.PAID,
                invoice.getStatus()
        );
    }

    @Test
    void shouldMarkIssuedInvoiceAsPaid() {
        Invoice invoice =
                createValidDraftInvoice();

        invoiceService.issue(invoice);

        Invoice result =
                invoiceService.markAsPaid(invoice);

        assertSame(
                invoice,
                result
        );

        assertEquals(
                InvoiceStatus.PAID,
                result.getStatus()
        );
    }

    @Test
    void shouldRejectPaymentOfDraftInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.markAsPaid(invoice)
        );

        assertEquals(
                InvoiceStatus.DRAFT,
                invoice.getStatus()
        );
    }

    @Test
    void shouldRejectPaymentOfCancelledInvoice() {
        Invoice invoice =
                createValidDraftInvoice();

        invoiceService.cancel(invoice);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.markAsPaid(invoice)
        );

        assertEquals(
                InvoiceStatus.CANCELLED,
                invoice.getStatus()
        );
    }

    private Invoice createValidDraftInvoice() {

        InvoiceItem item =
                InvoiceItem.builder()
                        .description("Consultation")
                        .quantity(BigDecimal.ONE)
                        .unitPrice(
                                new BigDecimal("100.00")
                        )
                        .discountRate(
                                new BigDecimal("0.10")
                        )
                        .taxRate(
                                new BigDecimal("0.19")
                        )
                        .netAmount(
                                money("100.00")
                        )
                        .discountAmount(
                                money("10.00")
                        )
                        .taxAmount(
                                money("17.10")
                        )
                        .totalAmount(
                                money("107.10")
                        )
                        .build();

        return Invoice.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(
                        List.of(item)
                )
                .netAmount(
                        money("100.00")
                )
                .discountAmount(
                        money("10.00")
                )
                .taxAmount(
                        money("17.10")
                )
                .totalAmount(
                        money("107.10")
                )
                .status(
                        InvoiceStatus.DRAFT
                )
                .build();
    }

    @Test
    void shouldCalculateDueDateUsingPaymentTermDays() {

        InvoiceNumberGenerator generator =
                () -> INVOICE_NUMBER;

        BillingProperties properties =
                new BillingProperties(
                        "EUR",
                        14
                );

        DefaultInvoiceService service =
                new DefaultInvoiceService(
                        generator,
                        properties,
                        new InvoiceValidator(),
                        FIXED_CLOCK
                );

        Invoice invoice =
                createValidDraftInvoice();

        Invoice result =
                service.issue(invoice);

        assertEquals(
                LocalDate.of(2026, 9, 18),
                result.getDueDate()
        );
    }

    @Test
    void shouldNotGenerateInvoiceNumberWhenInvoiceIsNull() {

        InvoiceNumberGenerator generator =
                mock(InvoiceNumberGenerator.class);

        BillingProperties properties =
                new BillingProperties(
                        "EUR",
                        7
                );

        DefaultInvoiceService service =
                new DefaultInvoiceService(
                        generator,
                        properties,
                        new InvoiceValidator(),
                        FIXED_CLOCK
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.issue(null)
        );

        verifyNoInteractions(generator);
    }

    private Money money(
            String amount
    ) {
        return Money.of(
                new BigDecimal(amount),
                EUR
        );
    }
}