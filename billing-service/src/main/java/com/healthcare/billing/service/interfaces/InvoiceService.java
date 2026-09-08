package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;

public interface InvoiceService {

    InvoiceResponse create(CreateInvoiceRequest request);

    InvoiceResponse issue(Long invoiceId);

    InvoiceResponse cancel(Long invoiceId);

    InvoiceResponse markAsPaid(Long invoiceId);

    InvoiceResponse getById(Long invoiceId);

    InvoiceResponse getByInvoiceNumber(String invoiceNumber);
}