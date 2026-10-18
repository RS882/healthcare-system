package com.healthcare.billing.persistence;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import com.healthcare.billing.service.InvoiceCreator;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "billing.invoice.lock-timeout-ms=500"
})
@ActiveProfiles({"test", "stub"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class InvoiceConfigurationVerificationIntegrationTest
        extends AbstractPostgreSQLContainerTest {

    @Autowired
    private InvoiceStore invoiceStore;

    @Autowired
    private InvoiceCreator invoiceCreator;

    @Autowired
    private InvoiceJpaRepository invoiceJpaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate transactionTemplate;


    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);
        invoiceJpaRepository.deleteAll();
    }

    @Test
    void configuration_verification() {

        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Long invoiceId = saved.getId();

        Invoice invoice = transactionTemplate.execute(
                status -> {
                    Invoice result = invoiceStore.findByIdWithBlock(saved.getId());

                    String actualTimeout = jdbcTemplate.queryForObject(
                            "SHOW lock_timeout",
                            String.class
                    );
                    assertEquals("500ms", actualTimeout);

                    return result;
                }
        );

        assertNotNull(invoice);
        assertEquals(invoiceId, invoice.getId());

    }

    private CreateInvoiceRequest create_request() {
        return CreateInvoiceRequest.builder()
                .patientId(100L)
                .medicalFacilityId(200L)
                .items(
                        List.of(
                                CreateInvoiceItemRequest.builder()
                                        .serviceId(1L)
                                        .quantity(
                                                new BigDecimal("1.0000")
                                        )
                                        .discountRate(
                                                new BigDecimal("0.000000")
                                        )
                                        .build(),

                                CreateInvoiceItemRequest.builder()
                                        .serviceId(3L)
                                        .quantity(
                                                new BigDecimal("2.0000")
                                        )
                                        .discountRate(
                                                new BigDecimal("0.100000")
                                        )
                                        .build()
                        )
                )
                .build();
    }
}
