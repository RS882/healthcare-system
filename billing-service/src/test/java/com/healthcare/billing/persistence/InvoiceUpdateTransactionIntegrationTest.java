package com.healthcare.billing.persistence;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceStateMachine;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import com.healthcare.billing.service.InvoiceCreator;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "stub"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class InvoiceUpdateTransactionIntegrationTest
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
    private InvoiceStateMachine invoiceStateMachine;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);
        invoiceJpaRepository.deleteAll();
    }

    @Test
    void update_should_require_existing_transaction() {

        Invoice savedInvoice = getSavedInvoice();

        assertThrows(IllegalTransactionStateException.class,
                () -> invoiceStore.update(savedInvoice));
    }

    @Test
    void update_should_persist_changes_after_commit() {

        Invoice savedInvoice = getSavedInvoice();

        transactionTemplate.execute(
                status -> {
                    invoiceStateMachine.changeState(
                            savedInvoice,
                            InvoiceEvent.ISSUE
                    );
                    invoiceStore.update(savedInvoice);
                    return null;
                }
        );

        Invoice updatedInvoice = invoiceStore.findById(savedInvoice.getId());

        assertNotNull(updatedInvoice);
        assertEquals(savedInvoice.getId(), updatedInvoice.getId());
        assertEquals(InvoiceStatus.ISSUED, updatedInvoice.getStatus());
        assertNotNull(savedInvoice.getInvoiceNumber());
        assertNotNull(savedInvoice.getIssuedDate());
        assertNotNull(savedInvoice.getDueDate());
        assertEquals(
                savedInvoice.getInvoiceNumber(),
                updatedInvoice.getInvoiceNumber()
        );

        assertEquals(
                savedInvoice.getIssuedDate(),
                updatedInvoice.getIssuedDate()
        );

        assertEquals(
                savedInvoice.getDueDate(),
                updatedInvoice.getDueDate()
        );
    }

    @Test
    void update_should_not_persist_changes_after_rollback() {

        Invoice savedInvoice = getSavedInvoice();

        Long invoiceId = savedInvoice.getId();
        String invoiceNumber = savedInvoice.getInvoiceNumber();
        LocalDate invoiceIssuedDate = savedInvoice.getIssuedDate();
        LocalDate invoiceDueDate = savedInvoice.getDueDate();

        assertNull(invoiceNumber);
        assertNull(invoiceIssuedDate);
        assertNull(invoiceDueDate);

        transactionTemplate.execute(
                status -> {
                    invoiceStateMachine.changeState(
                            savedInvoice,
                            InvoiceEvent.ISSUE
                    );
                    invoiceStore.update(savedInvoice);

                    assertEquals(InvoiceStatus.ISSUED, savedInvoice.getStatus());
                    assertNotNull(savedInvoice.getInvoiceNumber());
                    assertNotNull(savedInvoice.getIssuedDate());
                    assertNotNull(savedInvoice.getDueDate());

                    status.setRollbackOnly();

                    return null;
                }
        );

        Invoice updatedInvoice = invoiceStore.findById(savedInvoice.getId());

        assertNotNull(updatedInvoice);

        assertEquals(invoiceId, updatedInvoice.getId());
        assertEquals(InvoiceStatus.DRAFT, updatedInvoice.getStatus());

        assertNull(updatedInvoice.getInvoiceNumber());
        assertNull(updatedInvoice.getIssuedDate());
        assertNull(updatedInvoice.getDueDate());
    }

    @Test
    void update_should_not_persist_changes_after_exception() {

        Invoice savedInvoice = getSavedInvoice();
        String exceptionMessage = "Simulated failure after invoice update";

        Long invoiceId = savedInvoice.getId();
        String invoiceNumber = savedInvoice.getInvoiceNumber();
        LocalDate invoiceIssuedDate = savedInvoice.getIssuedDate();
        LocalDate invoiceDueDate = savedInvoice.getDueDate();

        assertNull(invoiceNumber);
        assertNull(invoiceIssuedDate);
        assertNull(invoiceDueDate);

        IllegalStateException exception =  assertThrows(IllegalStateException.class,
                () -> transactionTemplate.execute(
                        status -> {
                            invoiceStateMachine.changeState(
                                    savedInvoice,
                                    InvoiceEvent.ISSUE
                            );
                            invoiceStore.update(savedInvoice);

                            assertEquals(InvoiceStatus.ISSUED, savedInvoice.getStatus());
                            assertNotNull(savedInvoice.getInvoiceNumber());
                            assertNotNull(savedInvoice.getIssuedDate());
                            assertNotNull(savedInvoice.getDueDate());

                            throw new IllegalStateException(exceptionMessage);

                        }
                )
        );

        assertEquals(
                exceptionMessage,
                exception.getMessage()
        );

        Invoice updatedInvoice = invoiceStore.findById(savedInvoice.getId());

        assertNotNull(updatedInvoice);

        assertEquals(invoiceId, updatedInvoice.getId());
        assertEquals(InvoiceStatus.DRAFT, updatedInvoice.getStatus());

        assertNull(updatedInvoice.getInvoiceNumber());
        assertNull(updatedInvoice.getIssuedDate());
        assertNull(updatedInvoice.getDueDate());
    }

    private Invoice getSavedInvoice() {
        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Long invoiceId = saved.getId();

        assertNotNull(invoiceId);
        assertTrue(invoiceId > 0);

        return saved;
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
