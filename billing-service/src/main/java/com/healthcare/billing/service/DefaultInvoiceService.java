package com.healthcare.billing.service;

import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import com.healthcare.billing.mapper.InvoiceMapper;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceStateMachine;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.persistence.InvoiceStore;
import com.healthcare.billing.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultInvoiceService implements InvoiceService {

    private final InvoiceStore invoiceStore;
    private final InvoiceStateMachine invoiceStateMachine;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceCreator invoiceCreator;

    @Transactional
    @Override
    public InvoiceResponse create(CreateInvoiceRequest request) {

        Invoice newDraftInvoice = invoiceCreator.createDraft(request);

        Invoice savedInvoice = invoiceStore.save(newDraftInvoice);

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Transactional
    @Override
    public InvoiceResponse issue(Long invoiceId) {

        Invoice invoice = invoiceStore.findById(invoiceId);

        invoiceStateMachine.changeState(
                invoice,
                InvoiceEvent.ISSUE
        );

        invoiceStore.update(invoice);

        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    @Override
    public InvoiceResponse cancel(Long invoiceId) {

        Invoice invoice = invoiceStore.findById(invoiceId);

        invoiceStateMachine.changeState(
                invoice,
                InvoiceEvent.CANCEL
        );

        invoiceStore.update(invoice);

        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    @Override
    public InvoiceResponse markAsPaid(Long invoiceId) {

        Invoice invoice = invoiceStore.findById(invoiceId);

        invoiceStateMachine.changeState(
                invoice,
                InvoiceEvent.PAY
        );

        invoiceStore.update(invoice);

        return invoiceMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    @Override
    public InvoiceResponse getById(Long invoiceId) {

        return invoiceMapper.toResponse(invoiceStore.findById(invoiceId));
    }

    @Transactional(readOnly = true)
    @Override
    public InvoiceResponse getByInvoiceNumber(String invoiceNumber) {

        return invoiceMapper.toResponse(
                invoiceStore.findByInvoiceNumber(invoiceNumber)
        );
    }
}