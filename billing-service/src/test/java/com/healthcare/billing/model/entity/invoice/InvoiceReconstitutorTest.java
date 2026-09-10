package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.exception.InvoicePersistenceMappingException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceReconstitutorTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final LocalDate ISSUED_DATE = LocalDate.of(2026, 9, 9);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 16);
    private static final String INVOICE_NUMBER = "INV-2026-000001";

    private InvoiceReconstitutor reconstitutor;

    @BeforeEach
    void setUp() {
        reconstitutor = new InvoiceReconstitutor();
    }

    @Test
    void shouldRestoreDraftInvoice() {
        Invoice invoice = restore(InvoiceStatus.DRAFT, null, null, null);

        assertAll(
                () -> assertEquals(1L, invoice.getId()),
                () -> assertEquals(InvoiceStatus.DRAFT, invoice.getStatus()),
                () -> assertNull(invoice.getInvoiceNumber()),
                () -> assertNull(invoice.getIssuedDate()),
                () -> assertNull(invoice.getDueDate())
        );
    }

    @Test
    void shouldRestoreIssuedInvoice() {
        Invoice invoice = restore(
                InvoiceStatus.ISSUED,
                INVOICE_NUMBER,
                ISSUED_DATE,
                DUE_DATE
        );

        assertAll(
                () -> assertEquals(InvoiceStatus.ISSUED, invoice.getStatus()),
                () -> assertEquals(INVOICE_NUMBER, invoice.getInvoiceNumber()),
                () -> assertEquals(ISSUED_DATE, invoice.getIssuedDate()),
                () -> assertEquals(DUE_DATE, invoice.getDueDate())
        );
    }

    @Test
    void shouldRestorePaidInvoice() {
        assertDoesNotThrow(() -> restore(
                InvoiceStatus.PAID,
                INVOICE_NUMBER,
                ISSUED_DATE,
                DUE_DATE
        ));
    }

    @Test
    void shouldRestoreCancelledInvoiceFromDraft() {
        assertDoesNotThrow(() -> restore(
                InvoiceStatus.CANCELLED,
                null,
                null,
                null
        ));
    }

    @Test
    void shouldRestoreCancelledInvoiceFromIssued() {
        assertDoesNotThrow(() -> restore(
                InvoiceStatus.CANCELLED,
                INVOICE_NUMBER,
                ISSUED_DATE,
                DUE_DATE
        ));
    }

    @Test
    void shouldRejectInvalidPersistedId() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> reconstitutor.restore(
                        null,
                        null,
                        1L,
                        10L,
                        List.of(validItem()),
                        money("100.00"),
                        money("10.00"),
                        money("17.10"),
                        money("107.10"),
                        InvoiceStatus.DRAFT,
                        null,
                        null
                )
        );

        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> reconstitutor.restore(
                        0L,
                        null,
                        1L,
                        10L,
                        List.of(validItem()),
                        money("100.00"),
                        money("10.00"),
                        money("17.10"),
                        money("107.10"),
                        InvoiceStatus.DRAFT,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectNullPersistedStatus() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> reconstitutor.restore(
                        1L,
                        null,
                        1L,
                        10L,
                        List.of(validItem()),
                        money("100.00"),
                        money("10.00"),
                        money("17.10"),
                        money("107.10"),
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectDraftWithIssueData() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.DRAFT, INVOICE_NUMBER, null, null)
        );
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.DRAFT, null, ISSUED_DATE, null)
        );
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.DRAFT, null, null, DUE_DATE)
        );
    }

    @Test
    void shouldRejectIssuedOrPaidWithoutCompleteIssueData() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.ISSUED, null, ISSUED_DATE, DUE_DATE)
        );
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.ISSUED, INVOICE_NUMBER, null, DUE_DATE)
        );
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.PAID, INVOICE_NUMBER, ISSUED_DATE, null)
        );
    }

    @Test
    void shouldRejectDueDateBeforeIssuedDateForIssuedState() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(
                        InvoiceStatus.ISSUED,
                        INVOICE_NUMBER,
                        ISSUED_DATE,
                        ISSUED_DATE.minusDays(1)
                )
        );
    }

    @Test
    void shouldRejectCancelledInvoiceWithPartialIssueData() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.CANCELLED, INVOICE_NUMBER, null, null)
        );
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(InvoiceStatus.CANCELLED, null, ISSUED_DATE, DUE_DATE)
        );
    }

    @Test
    void shouldRejectCancelledFromIssuedWithInvalidDueDate() {
        assertThrows(
                InvoicePersistenceMappingException.class,
                () -> restore(
                        InvoiceStatus.CANCELLED,
                        INVOICE_NUMBER,
                        ISSUED_DATE,
                        ISSUED_DATE.minusDays(1)
                )
        );
    }

    private Invoice restore(
            InvoiceStatus status,
            String invoiceNumber,
            LocalDate issuedDate,
            LocalDate dueDate
    ) {
        return reconstitutor.restore(
                1L,
                invoiceNumber,
                1L,
                10L,
                List.of(validItem()),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10"),
                status,
                issuedDate,
                dueDate
        );
    }

    private InvoiceItem validItem() {
        return InvoiceItem.builder()
                .serviceId(1L)
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

    private Money money(String value) {
        return Money.of(new BigDecimal(value), EUR);
    }
}
