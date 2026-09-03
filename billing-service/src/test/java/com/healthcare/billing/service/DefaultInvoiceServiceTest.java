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

class DefaultInvoiceServiceTest {

    private static final Currency EUR =
            Currency.getInstance("EUR");

    private static final ZoneId ZONE =
            ZoneId.of("Europe/Berlin");

    private DefaultInvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        InvoiceNumberGenerator invoiceNumberGenerator =
                () -> "INV-2026-000001";

        BillingProperties billingProperties =
                new BillingProperties(
                        "EUR",
                        7
                );

        Clock clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-09-03T10:00:00Z"
                        ),
                        ZONE
                );

        InvoiceValidator invoiceValidator = new InvoiceValidator();

        invoiceService =
                new DefaultInvoiceService(
                        invoiceNumberGenerator,
                        billingProperties,
                        invoiceValidator,
                        clock
                );
    }

    @Test
    void shouldIssueDraftInvoice() {
        Invoice invoice = createValidDraftInvoice();

        Invoice result =
                invoiceService.issue(invoice);

        assertSame(
                invoice,
                result
        );

        assertEquals(
                InvoiceStatus.ISSUED,
                result.getStatus()
        );

        assertEquals(
                "INV-2026-000001",
                result.getInvoiceNumber()
        );

        assertEquals(
                LocalDate.of(2026, 9, 3),
                result.getIssuedDate()
        );

        assertEquals(
                LocalDate.of(2026, 9, 10),
                result.getDueDate()
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
        Invoice invoice = createValidDraftInvoice();

        invoiceService.issue(invoice);

        assertThrows(
                IllegalStateException.class,
                () -> invoiceService.issue(invoice)
        );
    }

    private Invoice createValidDraftInvoice() {
        InvoiceItem item =
                InvoiceItem.builder()
                        .description("Consultation")
                        .quantity(BigDecimal.ONE)
                        .unitPrice(new BigDecimal("100.00"))
                        .discountRate(new BigDecimal("0.10"))
                        .taxRate(new BigDecimal("0.19"))
                        .netAmount(
                                Money.of(
                                        new BigDecimal("100.00"),
                                        EUR
                                )
                        )
                        .discountAmount(
                                Money.of(
                                        new BigDecimal("10.00"),
                                        EUR
                                )
                        )
                        .taxAmount(
                                Money.of(
                                        new BigDecimal("17.10"),
                                        EUR
                                )
                        )
                        .totalAmount(
                                Money.of(
                                        new BigDecimal("107.10"),
                                        EUR
                                )
                        )
                        .build();

        return Invoice.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(List.of(item))
                .netAmount(
                        Money.of(
                                new BigDecimal("100.00"),
                                EUR
                        )
                )
                .discountAmount(
                        Money.of(
                                new BigDecimal("10.00"),
                                EUR
                        )
                )
                .taxAmount(
                        Money.of(
                                new BigDecimal("17.10"),
                                EUR
                        )
                )
                .totalAmount(
                        Money.of(
                                new BigDecimal("107.10"),
                                EUR
                        )
                )
                .status(InvoiceStatus.DRAFT)
                .build();
    }
}