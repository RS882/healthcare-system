package com.healthcare.billing.service;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import com.healthcare.billing.validation.InvoiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceCalculatorTest {

    private static final Long SERVICE_ID = 1L;
    private static final Currency EUR = Currency.getInstance("EUR");

    private InvoiceItemCalculator itemCalculator;
    private InvoiceCalculator invoiceCalculator;

    @BeforeEach
    void setUp() {

        BillingProperties properties = new BillingProperties("EUR", 7);

        InvoiceValidator validator = new InvoiceValidator();

        MoneyPolicy moneyPolicy = new MoneyPolicy(properties);

        itemCalculator = new InvoiceItemCalculator(moneyPolicy);

        invoiceCalculator = new InvoiceCalculator(moneyPolicy, validator);
    }

    @Test
    void shouldCalculateInvoiceWithDifferentDiscountAndTaxRates() {

        InvoiceItem consultation =
                itemCalculator.calculate(
                        1L,
                        "Consultation",
                        BigDecimal.ONE,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.10"),
                        new BigDecimal("0.19")
                );

        InvoiceItem laboratory =
                itemCalculator.calculate(
                        2L,
                        "Laboratory",
                        BigDecimal.ONE,
                        new BigDecimal("50.00"),
                        new BigDecimal("0.05"),
                        new BigDecimal("0.07")
                );

        Invoice invoice =
                invoiceCalculator.calculate(
                        1L,
                        10L,
                        List.of(
                                consultation,
                                laboratory
                        )
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

        Invoice invoice =
                invoiceCalculator.calculate(
                        1L,
                        10L,
                        List.of(createItem())
                );

        assertEquals(
                InvoiceStatus.DRAFT,
                invoice.getStatus()
        );
    }

    @Test
    void shouldNotAssignInvoiceNumberForDraft() {

        Invoice invoice =
                invoiceCalculator.calculate(
                        1L,
                        10L,
                        List.of(createItem())
                );

        assertNull(
                invoice.getInvoiceNumber()
        );
    }

    @Test
    void shouldNotAssignIssuedDateForDraft() {

        Invoice invoice =
                invoiceCalculator.calculate(
                        1L,
                        10L,
                        List.of(createItem())
                );

        assertNull(
                invoice.getIssuedDate()
        );
    }

    @Test
    void shouldNotAssignDueDateForDraft() {

        Invoice invoice =
                invoiceCalculator.calculate(
                        1L,
                        10L,
                        List.of(createItem())
                );

        assertNull(
                invoice.getDueDate()
        );
    }

    @Test
    void shouldPreservePatientAndMedicalFacilityIds() {

        Invoice invoice =
                invoiceCalculator.calculate(
                        25L,
                        100L,
                        List.of(createItem())
                );

        assertEquals(
                25L,
                invoice.getPatientId()
        );

        assertEquals(
                100L,
                invoice.getMedicalFacilityId()
        );
    }

    @Test
    void shouldRejectNullItems() {

        assertThrows(
                InvoiceValidationException.class,
                () -> invoiceCalculator.calculate(
                        1L,
                        10L,
                        null
                )
        );
    }

    @Test
    void shouldRejectEmptyItems() {

        assertThrows(
                InvoiceValidationException.class,
                () -> invoiceCalculator.calculate(
                        1L,
                        10L,
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectListContainingNull() {

        List<InvoiceItem> items =
                new ArrayList<>();

        items.add(createItem());
        items.add(null);

        assertThrows(
                InvoiceValidationException.class,
                () -> invoiceCalculator.calculate(
                        1L,
                        10L,
                        items
                )
        );
    }

    @Test
    void shouldStoreImmutableCopyOfItems() {

        List<InvoiceItem> items =
                new ArrayList<>();

        items.add(createItem());

        Invoice invoice =
                invoiceCalculator.calculate(
                        1L,
                        10L,
                        items
                );

        items.clear();

        assertEquals(
                1,
                invoice.getItems().size()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> invoice.getItems().clear()
        );
    }

    private InvoiceItem createItem() {

        return itemCalculator.calculate(
                SERVICE_ID,
                "Consultation",
                BigDecimal.ONE,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("0.19")
        );
    }

    @Test
    void should_keep_invoice_total_consistent_with_summed_item_components() {
        InvoiceItem firstItem = InvoiceItem.builder()
                .serviceId(1L)
                .description("First service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("0.005"))
                .discountRate(new BigDecimal("0.07"))
                .taxRate(new BigDecimal("0.07"))
                .netAmount(Money.of("0.01", EUR))
                .discountAmount(Money.of("0.00", EUR))
                .taxAmount(Money.of("0.00", EUR))
                .totalAmount(Money.of("0.01", EUR))
                .build();

        InvoiceItem secondItem = InvoiceItem.builder()
                .serviceId(2L)
                .description("Second service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("10.00"))
                .discountRate(new BigDecimal("0.10"))
                .taxRate(new BigDecimal("0.19"))
                .netAmount(Money.of("10.00", EUR))
                .discountAmount(Money.of("1.00", EUR))
                .taxAmount(Money.of("1.71", EUR))
                .totalAmount(Money.of("10.71", EUR))
                .build();

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(firstItem, secondItem)
        );

        Money expectedTotal = invoice.getNetAmount()
                .subtract(invoice.getDiscountAmount())
                .add(invoice.getTaxAmount());

        assertThat(invoice.getNetAmount())
                .isEqualTo(Money.of("10.01", EUR));

        assertThat(invoice.getDiscountAmount())
                .isEqualTo(Money.of("1.00", EUR));

        assertThat(invoice.getTaxAmount())
                .isEqualTo(Money.of("1.71", EUR));

        assertThat(invoice.getTotalAmount())
                .isEqualTo(Money.of("10.72", EUR));

        assertThat(invoice.getTotalAmount())
                .isEqualTo(expectedTotal);
    }

    @Test
    void should_sum_rounded_item_taxes_instead_of_recalculating_invoice_tax() {
        InvoiceItem firstItem = InvoiceItem.builder()
                .serviceId(1L)
                .description("First service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("0.03"))
                .discountRate(BigDecimal.ZERO)
                .taxRate(new BigDecimal("0.19"))
                .netAmount(Money.of("0.03", EUR))
                .discountAmount(Money.of("0.00", EUR))
                .taxAmount(Money.of("0.01", EUR))
                .totalAmount(Money.of("0.04", EUR))
                .build();

        InvoiceItem secondItem = InvoiceItem.builder()
                .serviceId(2L)
                .description("Second service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("0.03"))
                .discountRate(BigDecimal.ZERO)
                .taxRate(new BigDecimal("0.19"))
                .netAmount(Money.of("0.03", EUR))
                .discountAmount(Money.of("0.00", EUR))
                .taxAmount(Money.of("0.01", EUR))
                .totalAmount(Money.of("0.04", EUR))
                .build();

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(firstItem, secondItem)
        );

        assertThat(invoice.getNetAmount())
                .isEqualTo(Money.of("0.06", EUR));

        assertThat(invoice.getTaxAmount())
                .isEqualTo(Money.of("0.02", EUR));

        assertThat(invoice.getTotalAmount())
                .isEqualTo(Money.of("0.08", EUR));
    }

    @Test
    void should_demonstrate_rounding_difference_between_line_tax_and_invoice_level_tax() {
        InvoiceItem firstItem = InvoiceItem.builder()
                .serviceId(1L)
                .description("First service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("0.03"))
                .discountRate(BigDecimal.ZERO)
                .taxRate(new BigDecimal("0.19"))
                .netAmount(Money.of("0.03", EUR))
                .discountAmount(Money.of("0.00", EUR))
                .taxAmount(Money.of("0.01", EUR))
                .totalAmount(Money.of("0.04", EUR))
                .build();

        InvoiceItem secondItem = InvoiceItem.builder()
                .serviceId(2L)
                .description("Second service")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("0.03"))
                .discountRate(BigDecimal.ZERO)
                .taxRate(new BigDecimal("0.19"))
                .netAmount(Money.of("0.03", EUR))
                .discountAmount(Money.of("0.00", EUR))
                .taxAmount(Money.of("0.01", EUR))
                .totalAmount(Money.of("0.04", EUR))
                .build();

        Invoice invoice = invoiceCalculator.calculate(
                1L,
                10L,
                List.of(firstItem, secondItem)
        );

        Money taxableAmount = invoice.getNetAmount()
                .subtract(invoice.getDiscountAmount());

        Money invoiceLevelTax = Money.of(
                taxableAmount.amount()
                        .multiply(new BigDecimal("0.19"))
                        .setScale(2, RoundingMode.HALF_UP),
                EUR
        );

        assertThat(invoice.getTaxAmount()).isEqualTo(Money.of("0.02", EUR));
        assertThat(invoiceLevelTax).isEqualTo(Money.of("0.01", EUR));

        assertThat(invoice.getTaxAmount()).isNotEqualTo(invoiceLevelTax);
    }
}