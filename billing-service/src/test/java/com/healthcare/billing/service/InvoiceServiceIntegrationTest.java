package com.healthcare.billing.service;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import com.healthcare.billing.exception.InvalidInvoiceTransitionException;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import com.healthcare.billing.service.interfaces.InvoiceService;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceServiceIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceJpaRepository invoiceJpaRepository;

    @BeforeEach
    void clean_database() {
        invoiceJpaRepository.deleteAll();
    }

    @Test
    void should_create_and_persist_draft_invoice() {

        InvoiceResponse created = invoiceService.create(create_request());

        assertThat(created.id()).isNotNull();

        assertThat(created.status()).isEqualTo(InvoiceStatus.DRAFT);

        assertThat(created.invoiceNumber()).isNull();
        assertThat(created.issuedDate()).isNull();
        assertThat(created.dueDate()).isNull();

        InvoiceResponse restored = invoiceService.getById(created.id());

        assertThat(restored.id()).isEqualTo(created.id());

        assertThat(restored.status()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void should_issue_draft_invoice_and_persist_issue_data() {

        InvoiceResponse created = invoiceService.create(create_request());

        InvoiceResponse issued = invoiceService.issue(created.id());

        assertThat(issued.status()).isEqualTo(InvoiceStatus.ISSUED);

        assertThat(issued.invoiceNumber()).isNotBlank();

        assertThat(issued.issuedDate()).isNotNull();

        assertThat(issued.dueDate()).isNotNull();

        assertThat(
                ChronoUnit.DAYS.between(
                        issued.issuedDate(),
                        issued.dueDate()
                )
        ).isEqualTo(7);

        InvoiceResponse restored = invoiceService.getById(created.id());

        assertThat(restored.status()).isEqualTo(InvoiceStatus.ISSUED);

        assertThat(restored.invoiceNumber()).isEqualTo(issued.invoiceNumber());
    }

    @Test
    void should_mark_issued_invoice_as_paid() {

        InvoiceResponse created = invoiceService.create(create_request());

        InvoiceResponse issued = invoiceService.issue(created.id());

        InvoiceResponse paid = invoiceService.markAsPaid(issued.id());

        assertThat(paid.status()).isEqualTo(InvoiceStatus.PAID);

        InvoiceResponse restored = invoiceService.getById(created.id());

        assertThat(restored.status()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void should_cancel_draft_invoice() {

        InvoiceResponse created = invoiceService.create(create_request());

        InvoiceResponse cancelled = invoiceService.cancel(created.id());

        assertThat(cancelled.status()).isEqualTo(InvoiceStatus.CANCELLED);

        assertThat(cancelled.invoiceNumber()).isNull();
        assertThat(cancelled.issuedDate()).isNull();
        assertThat(cancelled.dueDate()).isNull();

        InvoiceResponse restored = invoiceService.getById(created.id());

        assertThat(restored.status()).isEqualTo(InvoiceStatus.CANCELLED);
    }

    @Test
    void should_cancel_issued_invoice_and_preserve_issue_data() {

        InvoiceResponse created = invoiceService.create(create_request());

        InvoiceResponse issued = invoiceService.issue(created.id());

        InvoiceResponse cancelled = invoiceService.cancel(created.id());

        assertThat(cancelled.status()).isEqualTo(InvoiceStatus.CANCELLED);

        assertThat(cancelled.invoiceNumber()).isEqualTo(issued.invoiceNumber());

        assertThat(cancelled.issuedDate()).isEqualTo(issued.issuedDate());

        assertThat(cancelled.dueDate()).isEqualTo(issued.dueDate());
    }

    @Test
    void should_not_change_persisted_state_when_transition_is_invalid() {

        InvoiceResponse created = invoiceService.create(create_request());

        assertThatThrownBy(() -> invoiceService.markAsPaid(created.id()))
                .isInstanceOf(InvalidInvoiceTransitionException.class);

        InvoiceResponse restored = invoiceService.getById(created.id());

        assertThat(restored.status()).isEqualTo(InvoiceStatus.DRAFT);

        assertThat(restored.invoiceNumber()).isNull();
        assertThat(restored.issuedDate()).isNull();
        assertThat(restored.dueDate()).isNull();
    }

    @Test
    void should_find_issued_invoice_by_invoice_number() {

        InvoiceResponse created = invoiceService.create(create_request());

        InvoiceResponse issued = invoiceService.issue(created.id());

        InvoiceResponse restored = invoiceService.getByInvoiceNumber(issued.invoiceNumber());

        assertThat(restored.id()).isEqualTo(issued.id());

        assertThat(restored.invoiceNumber()).isEqualTo(issued.invoiceNumber());
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
                                        .build()
                        )
                )
                .build();
    }
}