package com.healthcare.billing.generator;


import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceNumberGeneratorIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private DatabaseInvoiceNumberGenerator invoiceNumberGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void reset_sequence() {
        jdbcTemplate.execute("ALTER SEQUENCE invoice_number_seq RESTART WITH 1");
    }

    @Test
    void should_generate_invoice_number_using_postgresql_sequence() {

        String invoiceNumber = invoiceNumberGenerator.nextNumber();

        assertThat(invoiceNumber).matches("INV-\\d{4}-000001");
    }

    @Test
    void should_increment_invoice_number_sequence() {

        String firstNumber = invoiceNumberGenerator.nextNumber();

        String secondNumber = invoiceNumberGenerator.nextNumber();

        String thirdNumber = invoiceNumberGenerator.nextNumber();

        assertThat(firstNumber).matches("INV-\\d{4}-000001");

        assertThat(secondNumber).matches("INV-\\d{4}-000002");

        assertThat(thirdNumber).matches("INV-\\d{4}-000003");
    }
}