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

        return changeState(invoiceId, InvoiceEvent.ISSUE);
    }

    @Transactional
    @Override
    public InvoiceResponse cancel(Long invoiceId) {

        return changeState(invoiceId, InvoiceEvent.CANCEL);
    }

    @Transactional
    @Override
    public InvoiceResponse markAsPaid(Long invoiceId) {

        return changeState(invoiceId, InvoiceEvent.PAY);
    }

    @Transactional(readOnly = true)
    @Override
    public InvoiceResponse getById(Long invoiceId) {

        return invoiceMapper.toResponse(invoiceStore.findById(invoiceId));
    }

    @Transactional(readOnly = true)
    @Override
    public InvoiceResponse getByInvoiceNumber(String invoiceNumber) {

        return invoiceMapper.toResponse(invoiceStore.findByInvoiceNumber(invoiceNumber)
        );
    }

    private InvoiceResponse changeState(Long id, InvoiceEvent event) {
        Invoice invoice = invoiceStore.findById(id);

        invoiceStateMachine.changeState(invoice, event);

        invoiceStore.update(invoice);

        return invoiceMapper.toResponse(invoice);
    }
}