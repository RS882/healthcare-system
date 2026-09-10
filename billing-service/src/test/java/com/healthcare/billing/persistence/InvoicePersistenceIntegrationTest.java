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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoicePersistenceIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private InvoiceStore invoiceStore;

    @Autowired
    private InvoiceCreator invoiceCreator;

    @Autowired
    private InvoiceStateMachine invoiceStateMachine;

    @Autowired
    private InvoiceJpaRepository invoiceJpaRepository;

    @BeforeEach
    void clean_database() {
        invoiceJpaRepository.deleteAll();
    }

    @Test
    void should_save_and_restore_draft_invoice_with_items() {

        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Invoice restored = invoiceStore.findById(saved.getId());

        assertThat(restored.getId()).isNotNull();
        assertThat(restored.getPatientId()).isEqualTo(100L);
        assertThat(restored.getMedicalFacilityId()).isEqualTo(200L);

        assertThat(restored.getStatus()).isEqualTo(InvoiceStatus.DRAFT);

        assertThat(restored.getInvoiceNumber()).isNull();
        assertThat(restored.getIssuedDate()).isNull();
        assertThat(restored.getDueDate()).isNull();

        assertThat(restored.getItems()).hasSize(2);

        assertThat(restored.getItems())
                .allSatisfy(item -> assertThat(item.getId()).isNotNull());

        assertThat(restored.getTotalAmount()).isEqualTo(saved.getTotalAmount());
    }

    @Test
    void should_update_invoice_lifecycle_data() {

        Invoice saved =
                invoiceStore.save(
                        invoiceCreator.createDraft(
                                create_request()
                        )
                );

        invoiceStateMachine.changeState(
                saved,
                InvoiceEvent.ISSUE
        );

        invoiceStore.update(saved);

        Invoice restored =
                invoiceStore.findById(saved.getId());

        assertThat(restored.getStatus())
                .isEqualTo(InvoiceStatus.ISSUED);

        assertThat(restored.getInvoiceNumber())
                .isNotBlank();

        assertThat(restored.getIssuedDate())
                .isNotNull();

        assertThat(restored.getDueDate())
                .isNotNull();

        assertThat(restored.getDueDate())
                .isEqualTo(
                        restored.getIssuedDate().plusDays(7)
                );
    }

    @Test
    void should_find_invoice_by_invoice_number() {

        Invoice saved =
                invoiceStore.save(
                        invoiceCreator.createDraft(
                                create_request()
                        )
                );

        invoiceStateMachine.changeState(
                saved,
                InvoiceEvent.ISSUE
        );

        invoiceStore.update(saved);

        Invoice restored =
                invoiceStore.findByInvoiceNumber(
                        saved.getInvoiceNumber()
                );

        assertThat(restored.getId())
                .isEqualTo(saved.getId());

        assertThat(restored.getInvoiceNumber())
                .isEqualTo(saved.getInvoiceNumber());

        assertThat(restored.getStatus())
                .isEqualTo(InvoiceStatus.ISSUED);
    }

    private CreateInvoiceRequest create_request() {
        return CreateInvoiceRequest.builder()
                .patientId(100L)
                .medicalFacilityId(200L)
                .items(
                        java.util.List.of(
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