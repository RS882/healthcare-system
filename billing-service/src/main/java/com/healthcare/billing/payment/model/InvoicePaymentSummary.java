package com.healthcare.billing.payment.model;

import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentCoverageStatus;
import lombok.Builder;

@Builder
public record InvoicePaymentSummary(
        Long invoiceId,
        InvoiceStatus invoiceStatus,
        PaymentCoverageStatus paymentCoverageStatus,
        Money totalAmount,
        Money paidAmount,
        PaymentBalance balance
) {
}
