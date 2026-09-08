package com.healthcare.billing.controller.API;


import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.healthcare.billing.controller.API.InvoiceApiPaths.*;


@RequestMapping(INVOICES_BASIC_URL)
public interface InvoiceAPI {

    @PostMapping
    ResponseEntity<InvoiceResponse> create(
            @Valid
            @RequestBody
            CreateInvoiceRequest request
    );

    @PatchMapping(ISSUE_BY_ID)
    ResponseEntity<InvoiceResponse> issue(
            @PathVariable(PATH_VARIABLE_ID)
            @NotNull
            @Positive
            Long id
    );

    @PatchMapping(CANCEL_BY_ID)
    ResponseEntity<InvoiceResponse> cancel(
            @PathVariable(PATH_VARIABLE_ID)
            @NotNull
            @Positive
            Long id
    );

    @PatchMapping(PAY_BY_ID)
    ResponseEntity<InvoiceResponse> pay(
            @PathVariable(PATH_VARIABLE_ID)
            @NotNull
            @Positive
            Long id
    );

    @GetMapping(GET_BY_ID)
    ResponseEntity<InvoiceResponse> getById(
            @PathVariable(PATH_VARIABLE_ID)
            @NotNull
            @Positive
            Long id
    );

    @GetMapping(GET_BY_INVOICE_NUMBER)
    ResponseEntity<InvoiceResponse> getByInvoiceNumber(
            @PathVariable(PATH_VARIABLE_INVOICE_NUMBER)
            @NotBlank
            String invoiceNumber
    );
}
