package com.healthcare.billing.service;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.generator.InvoiceNumberGenerator;
import com.healthcare.billing.model.entity.Invoice;
import com.healthcare.billing.service.interfaces.InvoiceService;
import com.healthcare.billing.validation.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DefaultInvoiceService implements InvoiceService {

    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final BillingProperties billingProperties;
    private final InvoiceValidator invoiceValidator;
    private final Clock clock;


    @Override
    public Invoice issue(Invoice invoice) {

        validateForIssue(invoice);

        LocalDate issuedDate = LocalDate.now(clock);

        LocalDate dueDate = issuedDate.plusDays(billingProperties.paymentTermDays());

        String invoiceNumber = invoiceNumberGenerator.nextNumber();

        invoiceValidator.validateForIssue(
                invoice,
                invoiceNumber,
                issuedDate,
                dueDate
        );

        invoice.issue(invoiceNumber, issuedDate, dueDate);

        return invoice;
    }

    private void validateForIssue(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice must not be null");
        }
    }
}