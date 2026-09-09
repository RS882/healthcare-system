package com.healthcare.billing.validation;

import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.value.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class InvoiceValidator {

    public void validateForPay(Invoice invoice) {
        validateInvoice(invoice);
        validateItems(invoice.getItems());
        validateAmounts(invoice);
        validateAmountConsistency(invoice);
    }

    public void validateForIssue(Invoice invoice) {
        validateInvoice(invoice);
        validateParties(invoice.getPatientId(), invoice.getMedicalFacilityId());
        validateItems(invoice.getItems());
        validateAmounts(invoice);
        validateAmountConsistency(invoice);
    }

    private void validateParties(
            Long patientId,
            Long medicalFacilityId
    ) {
        if (patientId == null) {
            throw new InvoiceValidationException("Patient id must not be null");
        }

        if (patientId <= 0) {
            throw new InvoiceValidationException("Patient id must be greater than zero");
        }

        if (medicalFacilityId == null) {
            throw new InvoiceValidationException("Medical facility id must not be null");
        }

        if (medicalFacilityId <= 0) {
            throw new InvoiceValidationException("Medical facility id must be greater than zero");
        }
    }

    public void validateItems(List<InvoiceItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvoiceValidationException("Invoice must contain at least one item");
        }

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new InvoiceValidationException("Invoice items must not contain null");
        }
    }

    private void validateAmounts(Invoice invoice) {
        Money netAmount = invoice.getNetAmount();
        Money discountAmount = invoice.getDiscountAmount();
        Money taxAmount = invoice.getTaxAmount();
        Money totalAmount = invoice.getTotalAmount();

        if (netAmount == null) {
            throw new InvoiceValidationException("Net amount must not be null");
        }

        if (discountAmount == null) {
            throw new InvoiceValidationException("Discount amount must not be null");
        }

        if (taxAmount == null) {
            throw new InvoiceValidationException("Tax amount must not be null");
        }

        if (totalAmount == null) {
            throw new InvoiceValidationException("Total amount must not be null");
        }

        if (discountAmount.isNegative()) {
            throw new InvoiceValidationException("Discount amount must not be negative");
        }

        if (taxAmount.isNegative()) {
            throw new InvoiceValidationException("Tax amount must not be negative");
        }

        if (totalAmount.isNegative()) {
            throw new InvoiceValidationException("Total amount must not be negative");
        }

        if (netAmount.isNegative()) {
            throw new InvoiceValidationException("Net amount must not be negative");
        }
    }

    private void validateAmountConsistency(Invoice invoice) {
        Money netAmount = invoice.getNetAmount();
        Money discountAmount = invoice.getDiscountAmount();
        Money taxAmount = invoice.getTaxAmount();
        Money totalAmount = invoice.getTotalAmount();

        Money expectedTotal = netAmount
                .subtract(discountAmount)
                .add(taxAmount);

        if (!expectedTotal.equals(totalAmount)) {
            throw new InvoiceValidationException("Invoice amounts are inconsistent");
        }
    }

    public void validateInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new InvoiceValidationException("Invoice must not be null");
        }
    }
}