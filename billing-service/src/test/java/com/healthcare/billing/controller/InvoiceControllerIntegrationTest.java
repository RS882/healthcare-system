package com.healthcare.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceControllerIntegrationTest extends AbstractPostgreSQLContainerTest {

    private static final String BASE_URL = "/v1/invoices";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceJpaRepository invoiceJpaRepository;

    @BeforeEach
    void clean_database() {
        invoiceJpaRepository.deleteAll();
    }

    @Test
    void should_create_invoice_via_rest_api() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create_request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.patientId").value(100))
                .andExpect(jsonPath("$.medicalFacilityId").value(200))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.invoiceNumber").doesNotExist());
    }

    @Test
    void should_execute_full_invoice_lifecycle_via_rest_api() throws Exception {

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create_request())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long invoiceId = objectMapper.readTree(createResponse).get("id").asLong();

        String issueResponse = mockMvc.perform(patch(BASE_URL + "/" + invoiceId + "/issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.invoiceNumber").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String invoiceNumber = objectMapper.readTree(issueResponse).get("invoiceNumber").asText();

        mockMvc.perform(get(BASE_URL + "/number/" + invoiceNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId))
                .andExpect(jsonPath("$.status").value("ISSUED"));

        mockMvc.perform(patch(BASE_URL + "/" + invoiceId + "/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get(BASE_URL + "/" + invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void should_return_validation_error_for_invalid_request() throws Exception {

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .patientId(-1L)
                .medicalFacilityId(null)
                .items(List.of())
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value(BASE_URL))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void should_return_invalid_request_body_for_malformed_json() throws Exception {

        String malformedJson =
                """
                        {
                          "patientId": 100,
                          "medicalFacilityId":
                        """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value("Request body is invalid"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    void should_return_invalid_request_parameter_for_wrong_id_type() throws Exception {

        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/abc"));
    }

    @Test
    void should_return_validation_error_for_non_positive_id() throws Exception {

        mockMvc.perform(get(BASE_URL + "/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/-1"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void should_return_not_found_for_unknown_invoice() throws Exception {

        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("INVOICE_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));
    }

    @Test
    void should_return_conflict_for_invalid_state_transition() throws Exception {

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create_request()))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long invoiceId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch(BASE_URL + "/" + invoiceId + "/pay"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("INVALID_INVOICE_TRANSITION"))
                .andExpect(
                        jsonPath("$.path").value(BASE_URL + "/" + invoiceId + "/pay"));

        mockMvc.perform(get(BASE_URL + "/" + invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    private CreateInvoiceRequest create_request() {
        return CreateInvoiceRequest.builder()
                .patientId(100L)
                .medicalFacilityId(200L)
                .items(List.of(CreateInvoiceItemRequest.builder()
                        .serviceId(1L)
                        .quantity(new BigDecimal("1.0000"))
                        .discountRate(new BigDecimal("0.000000"))
                        .build()))
                .build();
    }
}