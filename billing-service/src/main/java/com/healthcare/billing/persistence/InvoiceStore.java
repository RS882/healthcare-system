package com.healthcare.billing.persistence;

import com.healthcare.billing.model.entity.invoice.Invoice;

public interface InvoiceStore {

    Invoice findById(Long id);

    Invoice findByInvoiceNumber(String invoiceNumber);

    Invoice save(Invoice invoice);

    void update(Invoice invoice);
}