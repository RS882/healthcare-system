package com.healthcare.billing.controller;

import com.healthcare.billing.controller.API.InvoiceAPI;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import com.healthcare.billing.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class InvoiceController implements InvoiceAPI {

    private final InvoiceService invoiceService;

    @Override
    public ResponseEntity<InvoiceResponse> create(CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.create(request));
    }

    @Override
    public ResponseEntity<InvoiceResponse> issue(Long id) {
        return ResponseEntity.ok(invoiceService.issue(id));
    }

    @Override
    public ResponseEntity<InvoiceResponse> cancel(Long id) {
        return ResponseEntity.ok(invoiceService.cancel(id));
    }

    @Override
    public ResponseEntity<InvoiceResponse> pay(Long id) {
        return ResponseEntity.ok(invoiceService.markAsPaid(id));
    }

    @Override
    public ResponseEntity<InvoiceResponse> getById(Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @Override
    public ResponseEntity<InvoiceResponse> getByInvoiceNumber(String invoiceNumber) {
        return ResponseEntity.ok(invoiceService.getByInvoiceNumber(invoiceNumber));
    }
}