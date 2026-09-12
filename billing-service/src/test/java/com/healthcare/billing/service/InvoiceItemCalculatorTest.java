package com.healthcare.billing.service;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.exception.BillingValidationException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceItemCalculatorTest {

    private static final Long SERVICE_ID = 1L;

    private static final Currency EUR = Currency.getInstance("EUR");

    private InvoiceItemCalculator calculator;

    @BeforeEach
    void setUp() {
        BillingProperties properties = new BillingProperties("EUR", 7);

        MoneyPolicy moneyPolicy = new MoneyPolicy(properties);

        calculator = new InvoiceItemCalculator(moneyPolicy);
    }

    @Test
    void shouldCalculateInvoiceItem() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "Consultation",
                new BigDecimal("1.5"),
                new BigDecimal("80.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        assertEquals(SERVICE_ID, item.getServiceId());

        assertEquals(
                "Consultation",
                item.getDescription()
        );

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("120.00")
                        )
        );

        assertEquals(
                0,
                item.getDiscountAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("0.00")
                        )
        );

        assertEquals(
                0,
                item.getTaxAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("22.80")
                        )
        );

        assertEquals(
                0,
                item.getTotalAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("142.80")
                        )
        );

        assertEquals(
                Currency.getInstance("EUR"),
                item.getTotalAmount().currency()
        );
    }

    @Test
    void shouldCalculateInvoiceItemWithDiscount() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                new BigDecimal("0.10"),
                new BigDecimal("0.19")
        );

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("100.00")
                        )
        );

        assertEquals(
                0,
                item.getDiscountAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("10.00")
                        )
        );

        assertEquals(
                0,
                item.getTaxAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("17.10")
                        )
        );

        assertEquals(
                0,
                item.getTotalAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("107.10")
                        )
        );
    }

    @Test
    void shouldCalculateItemWithoutTax() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "Free tax service",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        assertTrue(item.getNetAmount().isZero() == false);
        assertTrue(item.getDiscountAmount().isZero());
        assertTrue(item.getTaxAmount().isZero());

        assertEquals(
                0,
                item.getTotalAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("100.00")
                        )
        );
    }

    @Test
    void shouldAllowZeroUnitPrice() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "Included service",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        assertTrue(item.getNetAmount().isZero());
        assertTrue(item.getDiscountAmount().isZero());
        assertTrue(item.getTaxAmount().isZero());
        assertTrue(item.getTotalAmount().isZero());
    }

    @Test
    void shouldPreserveUnitPricePrecisionUntilFinalCalculation() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "Medical material",
                new BigDecimal("100"),
                new BigDecimal("0.075"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("7.50")
                        )
        );
    }

    @Test
    void shouldCalculateFractionalDiscountRate() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "Medical material",
                new BigDecimal("100"),
                new BigDecimal("0.075"),
                new BigDecimal("0.075"),
                BigDecimal.ZERO
        );

        assertEquals(
                0,
                item.getNetAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("7.50")
                        )
        );

        assertEquals(
                0,
                item.getDiscountAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("0.56")
                        )
        );

        assertEquals(
                0,
                item.getTotalAmount()
                        .amount()
                        .compareTo(
                                new BigDecimal("6.94")
                        )
        );
    }

    @Test
    void shouldStripDescription() {

        InvoiceItem item = calculator.calculate(
                SERVICE_ID,
                "   Consultation   ",
                BigDecimal.ONE,
                new BigDecimal("80.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );

        assertEquals(
                "Consultation",
                item.getDescription()
        );
    }

    @Test
    void shouldRejectNullServiceId() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        null,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNonPositiveServiceId() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        0L,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNullDescription() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        null,
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectBlankDescription() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "   ",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        BigDecimal.ZERO,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        new BigDecimal("-1"),
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeUnitPrice() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("-80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeDiscountRate() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        new BigDecimal("-0.01"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectDiscountRateGreaterThanOne() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        new BigDecimal("1.01"),
                        new BigDecimal("0.19")
                )
        );
    }

    @Test
    void shouldRejectNegativeTaxRate() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("-0.01")
                )
        );
    }

    @Test
    void shouldRejectTaxRateGreaterThanOne() {

        assertThrows(
                BillingValidationException.class,
                () -> calculator.calculate(
                        SERVICE_ID,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("80.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("1.01")
                )
        );
    }

    @ParameterizedTest
    @CsvSource({
            "3, 0.335, 0.10, 0.19",
            "7, 0.333, 0.07, 0.19",
            "1.5, 12.345, 0.05, 0.19",
            "2.75, 19.995, 0.15, 0.07",
            "11, 0.095, 0.03, 0.19"
    })
    void should_keep_total_consistent_with_rounded_components(
            String quantity,
            String unitPrice,
            String discountRate,
            String taxRate
    ) {
        InvoiceItem item = calculator.calculate(
                1L,
                "Test service",
                new BigDecimal(quantity),
                new BigDecimal(unitPrice),
                new BigDecimal(discountRate),
                new BigDecimal(taxRate)
        );

        Money expectedTotal = item.getNetAmount()
                .subtract(item.getDiscountAmount())
                .add(item.getTaxAmount());

        assertThat(item.getTotalAmount()).isEqualTo(expectedTotal);
    }

    @Test
    void should_keep_total_consistent_after_rounding() {
        InvoiceItem item = calculator.calculate(
                1L,
                "Test service",
                new BigDecimal("1"),
                new BigDecimal("0.005"),
                new BigDecimal("0.07"),
                new BigDecimal("0.07")
        );

        Money totalFromComponents = item.getNetAmount()
                .subtract(item.getDiscountAmount())
                .add(item.getTaxAmount());

        assertThat(item.getNetAmount()).isEqualTo(Money.of("0.01", EUR));
        assertThat(item.getDiscountAmount()).isEqualTo(Money.of("0.00", EUR));
        assertThat(item.getTaxAmount()).isEqualTo(Money.of("0.00", EUR));
        assertThat(item.getTotalAmount()).isEqualTo(Money.of("0.01", EUR));

        assertThat(item.getTotalAmount()).isEqualTo(totalFromComponents);
    }
}