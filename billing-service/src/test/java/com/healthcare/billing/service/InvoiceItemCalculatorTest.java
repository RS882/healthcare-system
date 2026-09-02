package com.healthcare.billing.service;


import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.money.MoneyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceItemCalculatorTest {

    private InvoiceItemCalculator calculator;

    @BeforeEach
    void setUp() {
        BillingProperties properties = new BillingProperties("EUR");

        MoneyPolicy moneyPolicy = new MoneyPolicy(properties);

        calculator = new InvoiceItemCalculator(moneyPolicy);
    }

    @Test
    void shouldCalculateInvoiceItem() {
        InvoiceItem item = calculator.calculate(
                "Consultation",
                new BigDecimal("1.5"),
                new BigDecimal("80.00"),
                new BigDecimal("0.19")
        );

        assertEquals("Consultation", item.getDescription());

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(new BigDecimal("120.00"))
        );

        assertEquals(
                0,
                item.getTaxAmount()
                        .amount()
                        .compareTo(new BigDecimal("22.80"))
        );

        assertEquals(
                0,
                item.getTotalAmount()
                        .amount()
                        .compareTo(new BigDecimal("142.80"))
        );

        assertEquals(
                Currency.getInstance("EUR"),
                item.getTotalAmount().currency()
        );
    }

    @Test
    void shouldCalculateItemWithoutTax() {
        InvoiceItem item = calculator.calculate(
                "Free tax service",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO
        );

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(new BigDecimal("100.00"))
        );

        assertEquals(
                0,
                item.getTaxAmount()
                        .amount()
                        .compareTo(new BigDecimal("0.00"))
        );

        assertEquals(
                0,
                item.getTotalAmount()
                        .amount()
                        .compareTo(new BigDecimal("100.00"))
        );
    }

    @Test
    void shouldAllowZeroUnitPrice() {
        InvoiceItem item = calculator.calculate(
                "Included service",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        assertTrue(item.getNetAmount().isZero());

        assertTrue(item.getTaxAmount().isZero());

        assertTrue(item.getTotalAmount().isZero());
    }

    @Test
    void shouldPreserveUnitPricePrecisionUntilFinalCalculation() {
        InvoiceItem item = calculator.calculate(
                "Medical material",
                new BigDecimal("100"),
                new BigDecimal("0.075"),
                BigDecimal.ZERO
        );

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(new BigDecimal("7.50"))
        );
    }

    @Test
    void shouldStripDescription() {
        InvoiceItem item = calculator.calculate(
                "   Consultation   ",
                BigDecimal.ONE,
                new BigDecimal("80.00"),
                new BigDecimal("0.19")
        );

        assertEquals("Consultation", item.getDescription());
    }

    @Test
    void shouldRejectNullDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        null,
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        "   ",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        "Consultation",
                        BigDecimal.ZERO,
                        new BigDecimal("80.00"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        "Consultation",
                        new BigDecimal("-1"),
                        new BigDecimal("80.00"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("-80.00"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeTaxRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        new BigDecimal("-0.01")
                )
        );
    }

    @Test
    void shouldRejectTaxRateGreaterThanOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        new BigDecimal("1.01")
                )
        );
    }
}