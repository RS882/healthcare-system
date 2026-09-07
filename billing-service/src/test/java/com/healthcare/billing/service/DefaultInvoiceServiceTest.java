package com.healthcare.billing.service;

import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceStateMachine;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultInvoiceServiceTest {

    private InvoiceStateMachine invoiceStateMachine;

    private DefaultInvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceStateMachine = mock(InvoiceStateMachine.class);

        invoiceService = new DefaultInvoiceService(invoiceStateMachine);
    }

    @Test
    void shouldDelegateIssueToStateMachine() {
        Invoice invoice = createInvoice();

        Invoice result = invoiceService.issue(invoice);

        verify(invoiceStateMachine).changeState(invoice, InvoiceEvent.ISSUE);

        assertSame(invoice, result);
    }

    @Test
    void shouldDelegateCancelToStateMachine() {
        Invoice invoice = createInvoice();

        Invoice result = invoiceService.cancel(invoice);

        verify(invoiceStateMachine).changeState(invoice, InvoiceEvent.CANCEL);

        assertSame(invoice, result);
    }

    @Test
    void shouldDelegatePaymentToStateMachine() {
        Invoice invoice = createInvoice();

        Invoice result = invoiceService.markAsPaid(invoice);

        verify(invoiceStateMachine).changeState(invoice, InvoiceEvent.PAY);

        assertSame(invoice, result);
    }

    private Invoice createInvoice() {
        return Invoice.builder()
                .build();
    }
}