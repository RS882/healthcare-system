package com.healthcare.billing.service.interfaces;

import com.healthcare.billing.payment.model.InvoicePaymentSummary;

public interface InvoicePaymentQueryService {

    InvoicePaymentSummary getPaymentSummary(Long invoiceId);
}
