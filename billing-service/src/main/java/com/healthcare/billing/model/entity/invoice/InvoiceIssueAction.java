package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.generator.InvoiceNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InvoiceIssueAction {

    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final BillingProperties billingProperties;
    private final Clock clock;

    public void execute(Invoice invoice) {
        LocalDate issuedDate = LocalDate.now(clock);

        LocalDate dueDate = issuedDate.plusDays(billingProperties.paymentTermDays());

        String invoiceNumber = invoiceNumberGenerator.nextNumber();

        invoice.applyIssueData(
                invoiceNumber,
                issuedDate,
                dueDate
        );
    }
}
