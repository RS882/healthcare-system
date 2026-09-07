package com.healthcare.billing.validation;

import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceValidatorTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private InvoiceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InvoiceValidator();
    }

    @Test
    void shouldValidateCorrectDraftInvoice() {
        Invoice invoice = createValidInvoice();

        assertDoesNotThrow(() -> validator.validateForIssue(invoice));
    }

    @Test
    void shouldValidateCorrectInvoiceForPay() {
        Invoice invoice = createValidInvoice();

        assertDoesNotThrow(() -> validator.validateForPay(invoice));
    }

    @Test
    void shouldRejectNullInvoiceForDraftValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateForIssue(null));
    }

    @Test
    void shouldRejectNullInvoiceForPayValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateForPay(null));
    }

    @Test
    void shouldRejectNullPatientId() {
        Invoice invoice = createInvoice(
                null,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectZeroPatientId() {
        Invoice invoice = createInvoice(
                0L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNegativePatientId() {
        Invoice invoice = createInvoice(
                -1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullMedicalFacilityId() {
        Invoice invoice = createInvoice(
                1L,
                null,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectZeroMedicalFacilityId() {
        Invoice invoice = createInvoice(
                1L,
                0L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNegativeMedicalFacilityId() {
        Invoice invoice = createInvoice(
                1L,
                -1L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullItems() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                null,
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectEmptyItems() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                List.of(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectItemsContainingNull() {
        List<InvoiceItem> items =
                new ArrayList<>();

        items.add(createValidItem());
        items.add(null);

        Invoice invoice = createInvoice(
                1L,
                10L,
                items,
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullNetAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                null,
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullDiscountAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                null,
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullTaxAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                null,
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullTotalAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                null
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNegativeNetAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("-100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNegativeDiscountAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("-10.00"),
                money("17.10"),
                money("127.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNegativeTaxAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("-17.10"),
                money("72.90")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNegativeTotalAmount() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("-107.10")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectInconsistentAmounts() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("200.00")
        );

        assertThrows(IllegalStateException.class, () -> validateDraft(invoice));
    }

    @Test
    void shouldRejectNullItemsForPay() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                null,
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );

        assertThrows(IllegalStateException.class, () -> validator.validateForPay(invoice));
    }

    @Test
    void shouldRejectInvalidAmountsForPay() {
        Invoice invoice = createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("200.00")
        );

        assertThrows(IllegalStateException.class, () -> validator.validateForPay(invoice));
    }

    private void validateDraft(Invoice invoice) {
        validator.validateForIssue(invoice);
    }

    private Invoice createValidInvoice() {
        return createInvoice(
                1L,
                10L,
                createValidItems(),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10")
        );
    }

    private Invoice createInvoice(
            Long patientId,
            Long medicalFacilityId,
            List<InvoiceItem> items,
            Money netAmount,
            Money discountAmount,
            Money taxAmount,
            Money totalAmount
    ) {
        return Invoice.builder()
                .patientId(patientId)
                .medicalFacilityId(medicalFacilityId)
                .items(items)
                .netAmount(netAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .build();
    }

    private List<InvoiceItem> createValidItems() {
        return List.of(createValidItem());
    }

    private InvoiceItem createValidItem() {
        return InvoiceItem.builder()
                .description("Consultation")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("100.00"))
                .discountRate(new BigDecimal("0.10"))
                .taxRate(new BigDecimal("0.19"))
                .netAmount(money("100.00"))
                .discountAmount(money("10.00"))
                .taxAmount(money("17.10"))
                .totalAmount(money("107.10"))
                .build();
    }

    private Money money(String amount) {
        return Money.of(new BigDecimal(amount), EUR);
    }
}