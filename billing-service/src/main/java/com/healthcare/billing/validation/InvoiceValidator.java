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
        validateId(invoice.getPatientId(), "Patient id");
        validateId(invoice.getMedicalFacilityId(), "Medical facility id");
        validateItems(invoice.getItems());
        validateAmounts(invoice);
        validateAmountConsistency(invoice);
    }

    private void validateId(Long id, String idName) {

        IdValidation.validateId(id, idName, InvoiceValidationException::new);

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

        validateAmount(netAmount, "Net amount");
        validateAmount(discountAmount, "Discount amount");
        validateAmount(taxAmount, "Tax amount");
        validateAmount(totalAmount, "Total amount");
    }

    private void validateAmount(Money amount, String amountName) {
        MoneyValidation.validateNonNegative(
                amount,
                amountName,
                InvoiceValidationException::new);
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