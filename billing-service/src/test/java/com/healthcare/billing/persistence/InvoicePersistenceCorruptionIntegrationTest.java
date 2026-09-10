package com.healthcare.billing.persistence;

import com.healthcare.billing.exception.InvoicePersistenceMappingException;
import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Transactional
class InvoicePersistenceCorruptionIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private JpaInvoiceStore invoiceStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean_database() {
        jdbcTemplate.execute("DELETE FROM invoice_item");
        jdbcTemplate.execute("DELETE FROM invoice");
    }

    @Test
    void should_throw_invalid_persisted_currency_when_currency_is_invalid() {
        Long invoiceId = insert_valid_draft_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET currency = ?
                WHERE id = ?
                """, "BAD", invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvalidPersistedCurrencyException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_issued_invoice_has_no_invoice_number() {
        Long invoiceId = insert_valid_issued_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET invoice_number = NULL
                WHERE id = ?
                """, invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_issued_invoice_has_no_issued_date() {
        Long invoiceId = insert_valid_issued_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET issued_date = NULL
                WHERE id = ?
                """, invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_issued_invoice_has_no_due_date() {
        Long invoiceId = insert_valid_issued_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET due_date = NULL
                WHERE id = ?
                """, invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_due_date_is_before_issued_date() {
        Long invoiceId = insert_valid_issued_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET issued_date = ?, due_date = ?
                WHERE id = ?
                """,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 9),
                invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_draft_invoice_contains_issue_data() {
        Long invoiceId = insert_valid_draft_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET invoice_number = ?, issued_date = ?, due_date = ?
                WHERE id = ?
                """,
                "INV-2026-999999",
                LocalDate.of(2026, 9, 9),
                LocalDate.of(2026, 9, 16),
                invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_cancelled_invoice_has_only_invoice_number() {
        Long invoiceId = insert_valid_cancelled_draft_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET invoice_number = ?
                WHERE id = ?
                """, "INV-2026-999999", invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_cancelled_invoice_has_only_issued_date() {
        Long invoiceId = insert_valid_cancelled_draft_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET issued_date = ?
                WHERE id = ?
                """, LocalDate.of(2026, 9, 9), invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    @Test
    void should_throw_persistence_mapping_exception_when_cancelled_invoice_has_only_due_date() {
        Long invoiceId = insert_valid_cancelled_draft_invoice();

        jdbcTemplate.update("""
                UPDATE invoice
                SET due_date = ?
                WHERE id = ?
                """, LocalDate.of(2026, 9, 16), invoiceId);

        assertThatThrownBy(() -> invoiceStore.findById(invoiceId))
                .isInstanceOf(InvoicePersistenceMappingException.class);
    }

    private Long insert_valid_draft_invoice() {
        return jdbcTemplate.queryForObject("""
                INSERT INTO invoice (
                    patient_id,
                    medical_facility_id,
                    currency,
                    net_amount,
                    discount_amount,
                    tax_amount,
                    total_amount,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                100L,
                200L,
                "EUR",
                100.00,
                0.00,
                19.00,
                119.00,
                "DRAFT");
    }

    private Long insert_valid_issued_invoice() {
        return jdbcTemplate.queryForObject("""
                INSERT INTO invoice (
                    invoice_number,
                    patient_id,
                    medical_facility_id,
                    currency,
                    net_amount,
                    discount_amount,
                    tax_amount,
                    total_amount,
                    status,
                    issued_date,
                    due_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                "INV-2026-000001",
                100L,
                200L,
                "EUR",
                100.00,
                0.00,
                19.00,
                119.00,
                "ISSUED",
                LocalDate.of(2026, 9, 9),
                LocalDate.of(2026, 9, 16));
    }

    private Long insert_valid_cancelled_draft_invoice() {
        return jdbcTemplate.queryForObject("""
                INSERT INTO invoice (
                    patient_id,
                    medical_facility_id,
                    currency,
                    net_amount,
                    discount_amount,
                    tax_amount,
                    total_amount,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                100L,
                200L,
                "EUR",
                100.00,
                0.00,
                19.00,
                119.00,
                "CANCELLED");
    }
}