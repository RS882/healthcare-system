package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.model.entity.Invoice;

public interface InvoiceService {

    Invoice issue(Invoice invoice);

    Invoice cancel(Invoice invoice);

    Invoice markAsPaid(Invoice invoice);
}
