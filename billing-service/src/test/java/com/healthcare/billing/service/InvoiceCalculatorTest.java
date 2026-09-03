package com.healthcare.billing.service;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.model.entity.Invoice;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.enums.InvoiceStatus;
import com.healthcare.billing.money.MoneyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceCalculatorTest {

    private InvoiceItemCalculator itemCalculator;
    private InvoiceCalculator invoiceCalculator;

    @BeforeEach
    void setUp() {
        BillingProperties properties = new BillingProperties("EUR", 7);

        MoneyPolicy moneyPolicy = new MoneyPolicy(properties);

        itemCalculator = new InvoiceItemCalculator(moneyPolicy);

        invoiceCalculator = new InvoiceCalculator(moneyPolicy);
    }

    @Test
    void shouldCalculateInvoiceWithDifferentDiscountAndTaxRates() {

        InvoiceItem consultation = itemCalculator.calculate(
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                new BigDecimal("0.10"),
                new BigDecimal("0.19")
        );

        InvoiceItem laboratory = itemCalculator.calculate(
                "Laboratory",
                BigDecimal.ONE,
                new BigDecimal("50.00"),
                new BigDecimal("0.05"),
                new BigDecimal("0.07")
        );

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(consultation, laboratory)
        );

        assertEquals(
                0,
                invoice.getNetAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("150.00")
                        )
        );

        assertEquals(
                0,
                invoice.getDiscountAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("12.50")
                        )
        );

        assertEquals(
                0,
                invoice.getTaxAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("20.43")
                        )
        );

        assertEquals(
                0,
                invoice.getTotalAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("157.93")
                        )
        );
    }

    @Test
    void shouldCreateInvoiceAsDraft() {

        InvoiceItem item = itemCalculator.calculate(
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(item)
        );

        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
    }

    @Test
    void shouldNotAssignInvoiceNumberForDraft() {

        InvoiceItem item = itemCalculator.calculate(
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(item)
        );

        assertNull(invoice.getInvoiceNumber());
    }

    @Test
    void shouldNotAssignIssuedDateForDraft() {

        InvoiceItem item = itemCalculator.calculate(
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(item)
        );

        assertNull(invoice.getIssuedDate());
    }

    @Test
    void shouldNotAssignDueDateForDraft() {

        InvoiceItem item = itemCalculator.calculate(
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(item)
        );

        assertNull(invoice.getDueDate());
    }

    @Test
    void shouldPreservePatientAndMedicalFacilityIds() {

        InvoiceItem item = itemCalculator.calculate(
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        Invoice invoice = invoiceCalculator.calculate(
                25L,
                100L,
                List.of(item)
        );

        assertEquals(25L, invoice.getPatientId()
        );

        assertEquals(100L, invoice.getMedicalFacilityId()
        );
    }

    @Test
    void shouldRejectNullItems() {

        assertThrows(IllegalArgumentException.class,
                () -> invoiceCalculator.calculate(1L, 10L, null));
    }

    @Test
    void shouldRejectEmptyItems() {

        assertThrows(IllegalArgumentException.class,
                () -> invoiceCalculator.calculate(1L, 10L, List.of()));
    }

    @Test
    void shouldRejectListContainingNull() {

        List<InvoiceItem> items = new ArrayList<>();

        items.add(itemCalculator.calculate(
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );

        items.add(null);

        assertThrows(IllegalArgumentException.class,
                () -> invoiceCalculator.calculate(1L, 10L, items));
    }

    @Test
    void shouldStoreImmutableCopyOfItems() {

        List<InvoiceItem> items = new ArrayList<>();

        items.add(itemCalculator.calculate(
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                items
        );

        items.clear();

        assertEquals(1, invoice.getItems().size());

        assertThrows(UnsupportedOperationException.class,
                () -> invoice.getItems().clear());
    }
}