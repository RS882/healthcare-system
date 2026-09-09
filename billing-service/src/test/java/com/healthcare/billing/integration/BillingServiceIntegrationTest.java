package com.healthcare.billing.integration;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import com.healthcare.billing.exception.InvalidInvoiceTransitionException;
import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.service.interfaces.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class BillingServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("billing_test")
                    .withUsername("billing")
                    .withPassword("billing");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> true);
    }

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE invoice_item, invoice RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("ALTER SEQUENCE invoice_number_seq RESTART WITH 1");
    }

    @Test
    void shouldPersistFullInvoiceLifecycle() {
        InvoiceResponse created = invoiceService.create(validRequest());

        assertAll(
                () -> assertNotNull(created.id()),
                () -> assertEquals(InvoiceStatus.DRAFT, created.status()),
                () -> assertNull(created.invoiceNumber()),
                () -> assertEquals(new BigDecimal("85.00"), created.netAmount()),
                () -> assertEquals(new BigDecimal("101.15"), created.totalAmount())
        );

        InvoiceResponse issued = invoiceService.issue(created.id());

        assertAll(
                () -> assertEquals(InvoiceStatus.ISSUED, issued.status()),
                () -> assertNotNull(issued.invoiceNumber()),
                () -> assertTrue(issued.invoiceNumber().matches("INV-\\d{4}-000001")),
                () -> assertNotNull(issued.issuedDate()),
                () -> assertNotNull(issued.dueDate()),
                () -> assertEquals(issued.issuedDate().plusDays(7), issued.dueDate())
        );

        InvoiceResponse byNumber =
                invoiceService.getByInvoiceNumber(issued.invoiceNumber());
        assertEquals(created.id(), byNumber.id());

        InvoiceResponse paid = invoiceService.markAsPaid(created.id());
        assertEquals(InvoiceStatus.PAID, paid.status());

        InvoiceResponse reloaded = invoiceService.getById(created.id());
        assertEquals(InvoiceStatus.PAID, reloaded.status());
        assertEquals(issued.invoiceNumber(), reloaded.invoiceNumber());
    }

    @Test
    void shouldPersistCancellationFromDraftAndRejectFurtherTransition() {
        InvoiceResponse created = invoiceService.create(validRequest());

        InvoiceResponse cancelled = invoiceService.cancel(created.id());

        assertAll(
                () -> assertEquals(InvoiceStatus.CANCELLED, cancelled.status()),
                () -> assertNull(cancelled.invoiceNumber()),
                () -> assertNull(cancelled.issuedDate()),
                () -> assertNull(cancelled.dueDate())
        );

        assertThrows(
                InvalidInvoiceTransitionException.class,
                () -> invoiceService.markAsPaid(created.id())
        );
    }

    @Test
    void shouldRejectCorruptedPersistedCurrency() {
        InvoiceResponse created = invoiceService.create(validRequest());
        jdbcTemplate.update(
                "UPDATE invoice SET currency = ? WHERE id = ?",
                "BAD",
                created.id()
        );

        assertThrows(
                InvalidPersistedCurrencyException.class,
                () -> invoiceService.getById(created.id())
        );
    }

    @Test
    void shouldReturnValidationErrorForInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 0,
                                  "medicalFacilityId": 10,
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/v1/invoices"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void shouldReturnInvalidRequestBodyForMalformedJson() throws Exception {
        mockMvc.perform(post("/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value("Request body is invalid"));
    }

    @Test
    void shouldReturnInvalidRequestParameterForWrongIdType() throws Exception {
        mockMvc.perform(get("/v1/invoices/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }

    @Test
    void shouldReturnValidationErrorForNonPositiveId() throws Exception {
        mockMvc.perform(get("/v1/invoices/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void shouldReturnNotFoundForMissingInvoice() throws Exception {
        mockMvc.perform(get("/v1/invoices/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("INVOICE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Invoice with id 999999 not found"));
    }

    @Test
    void shouldReturnConflictForForbiddenTransition() throws Exception {
        InvoiceResponse created = invoiceService.create(validRequest());

        mockMvc.perform(patch("/v1/invoices/{id}/pay", created.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_INVOICE_TRANSITION"))
                .andExpect(jsonPath("$.message")
                        .value("Transition from DRAFT by event PAY is not allowed"));
    }

    private CreateInvoiceRequest validRequest() {
        return CreateInvoiceRequest.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(List.of(
                        CreateInvoiceItemRequest.builder()
                                .serviceId(1L)
                                .quantity(BigDecimal.ONE)
                                .discountRate(BigDecimal.ZERO)
                                .build()
                ))
                .build();
    }
}
