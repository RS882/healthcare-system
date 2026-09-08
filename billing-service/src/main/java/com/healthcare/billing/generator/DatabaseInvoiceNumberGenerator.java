package com.healthcare.billing.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DatabaseInvoiceNumberGenerator
        implements InvoiceNumberGenerator {

    private static final String NEXT_SEQUENCE_VALUE_SQL = "SELECT nextval('invoice_number_seq')";

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Override
    public String nextNumber() {

        Long sequenceValue = jdbcTemplate.queryForObject(
                NEXT_SEQUENCE_VALUE_SQL,
                Long.class
        );

        if (sequenceValue == null) {
            throw new IllegalStateException("Failed to obtain next invoice number sequence value");
        }

        int year = LocalDate.now(clock).getYear();

        return "INV-%d-%06d".formatted(year, sequenceValue);
    }
}
