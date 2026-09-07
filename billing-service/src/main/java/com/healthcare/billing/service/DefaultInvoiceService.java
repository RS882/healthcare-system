package com.healthcare.billing.service;

import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceStateMachine;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultInvoiceService implements InvoiceService {


    private final InvoiceStateMachine invoiceStateMachine;


    @Override
    public Invoice issue(Invoice invoice) {

        invoiceStateMachine.changeState(
                invoice,
                InvoiceEvent.ISSUE
        );

        return invoice;
    }

    @Override
    public Invoice cancel(Invoice invoice) {


        invoiceStateMachine.changeState(
                invoice,
                InvoiceEvent.CANCEL
        );

        return invoice;
    }

    @Override
    public Invoice markAsPaid(Invoice invoice) {


        invoiceStateMachine.changeState(
                invoice,
                InvoiceEvent.PAY
        );

        return invoice;
    }

}