package com.healthcare.billing.generator;

import com.healthcare.billing.exception.InvoiceNumberGenerationException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DatabaseInvoiceNumberGeneratorTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-09T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldGenerateReadableInvoiceNumber() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
                "SELECT nextval('invoice_number_seq')",
                Long.class
        )).thenReturn(42L);

        DatabaseInvoiceNumberGenerator generator =
                new DatabaseInvoiceNumberGenerator(jdbcTemplate, FIXED_CLOCK);

        assertEquals("INV-2026-000042", generator.nextNumber());
    }

    @Test
    void shouldRejectNullSequenceValue() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class)))
                .thenReturn(null);

        DatabaseInvoiceNumberGenerator generator =
                new DatabaseInvoiceNumberGenerator(jdbcTemplate, FIXED_CLOCK);

        assertThrows(InvoiceNumberGenerationException.class, generator::nextNumber);
    }
}
