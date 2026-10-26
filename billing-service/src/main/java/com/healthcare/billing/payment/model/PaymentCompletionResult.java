package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.exception.PaymentReconstitutionException;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.validation.IdValidation;
import lombok.Getter;

import java.time.Instant;


public record PaymentCompletionResult<T extends PaymentMethodDetails>(
        Long paymentId,
        Long invoiceId,
        PaymentStatus paymentStatus,
        InvoiceStatus invoiceStatus,
        Instant completedAt,
        T methodDetails
) {

    public PaymentCompletionResult {

        validateId(paymentId, IdType.PAYMENT_ID);
        validateId(invoiceId, IdType.INVOICE_ID);

        validatePaymentStatus(paymentStatus);
        validateInvoiceStatus(invoiceStatus);

        validateCompleteAt(completedAt);
        validatePaymentMethodDetails(methodDetails);

    }

    private static void validateId(Long id, IdType idType) {

        IdValidation.validateId(id, idType.getValue(), PaymentDetailsValidationException::new);

    }

    private static void validatePaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new PaymentDetailsValidationException("Payment status cannot be null");
        }
        if (paymentStatus != PaymentStatus.COMPLETED) {
            throw new PaymentDetailsValidationException(
                    "Payment status must be %s".formatted(PaymentStatus.COMPLETED.name())
            );
        }
    }

    private static void validateInvoiceStatus(InvoiceStatus invoiceStatus) {
        if (invoiceStatus == null) {
            throw new InvoiceValidationException("Invoice status cannot be null");
        }

        if (invoiceStatus != InvoiceStatus.ISSUED && invoiceStatus != InvoiceStatus.PAID) {
            throw new InvoiceValidationException(
                    "Invoice status must be %s or %s".formatted(
                            InvoiceStatus.ISSUED.name(),
                            InvoiceStatus.PAID.name()
                    )
            );
        }
    }

    private static void validateCompleteAt(Instant completeAt) {
        if (completeAt == null) {
            throw new PaymentDetailsValidationException("Complete at time cannot be null");
        }
    }

    private static void validatePaymentMethodDetails(PaymentMethodDetails paymentMethodDetails) {
        if (paymentMethodDetails == null) {
            throw new PaymentDetailsValidationException("Payment method details cannot be null");
        }
    }

    @Getter
    private enum IdType {
        PAYMENT_ID("Payment"),
        INVOICE_ID("Invoice");

        private final String value;

        IdType(String value) {
            this.value = value;
        }
    }
}

