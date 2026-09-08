package com.healthcare.billing.service;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.dto.invoice.InvoiceResponse;
import com.healthcare.billing.mapper.InvoiceMapper;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceStateMachine;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.persistence.InvoiceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class DefaultInvoiceServiceTest {

    private InvoiceStore invoiceStore;
    private InvoiceStateMachine invoiceStateMachine;
    private InvoiceMapper invoiceMapper;
    private InvoiceCreator invoiceCreator;

    private DefaultInvoiceService invoiceService;

    @BeforeEach
    void setUp() {

        invoiceStore = mock(InvoiceStore.class);

        invoiceStateMachine = mock(InvoiceStateMachine.class);

        invoiceMapper = mock(InvoiceMapper.class);

        invoiceCreator = mock(InvoiceCreator.class);

        invoiceService = new DefaultInvoiceService(
                invoiceStore,
                invoiceStateMachine,
                invoiceMapper,
                invoiceCreator
        );
    }

    @Test
    void shouldCreateAndSaveDraftInvoice() {

        CreateInvoiceRequest request = createRequest();

        Invoice draftInvoice = createInvoice();

        Invoice savedInvoice = createInvoice();

        InvoiceResponse response = InvoiceResponse.builder().build();

        when(invoiceCreator.createDraft(request)).thenReturn(draftInvoice);

        when(invoiceStore.save(draftInvoice)).thenReturn(savedInvoice);

        when(invoiceMapper.toResponse(savedInvoice)).thenReturn(response);

        InvoiceResponse result = invoiceService.create(request);

        verify(invoiceCreator).createDraft(request);

        verify(invoiceStore).save(draftInvoice);

        verify(invoiceMapper).toResponse(savedInvoice);

        assertSame(response, result);
    }

    @Test
    void shouldIssueInvoice() {

        Long invoiceId = 1L;

        Invoice invoice = createInvoice();

        InvoiceResponse response = InvoiceResponse.builder().build();

        when(invoiceStore.findById(invoiceId)).thenReturn(invoice);

        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        InvoiceResponse result = invoiceService.issue(invoiceId);

        verify(invoiceStore).findById(invoiceId);

        verify(invoiceStateMachine)
                .changeState(
                        invoice,
                        InvoiceEvent.ISSUE
                );

        verify(invoiceStore).update(invoice);

        verify(invoiceMapper).toResponse(invoice);

        assertSame(response, result);
    }

    @Test
    void shouldCancelInvoice() {

        Long invoiceId = 1L;

        Invoice invoice = createInvoice();

        InvoiceResponse response = InvoiceResponse.builder().build();

        when(invoiceStore.findById(invoiceId)).thenReturn(invoice);

        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        InvoiceResponse result = invoiceService.cancel(invoiceId);

        verify(invoiceStore).findById(invoiceId);

        verify(invoiceStateMachine).changeState(
                invoice,
                InvoiceEvent.CANCEL
        );

        verify(invoiceStore).update(invoice);

        verify(invoiceMapper).toResponse(invoice);

        assertSame(response, result);
    }

    @Test
    void shouldMarkInvoiceAsPaid() {

        Long invoiceId = 1L;

        Invoice invoice = createInvoice();

        InvoiceResponse response = InvoiceResponse.builder().build();

        when(invoiceStore.findById(invoiceId)).thenReturn(invoice);

        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        InvoiceResponse result = invoiceService.markAsPaid(invoiceId);

        verify(invoiceStore).findById(invoiceId);

        verify(invoiceStateMachine).changeState(invoice, InvoiceEvent.PAY);

        verify(invoiceStore).update(invoice);

        verify(invoiceMapper).toResponse(invoice);

        assertSame(response, result);
    }

    @Test
    void shouldGetInvoiceById() {

        Long invoiceId = 1L;

        Invoice invoice = createInvoice();

        InvoiceResponse response = InvoiceResponse.builder().build();

        when(invoiceStore.findById(invoiceId)).thenReturn(invoice);

        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        InvoiceResponse result = invoiceService.getById(invoiceId);

        verify(invoiceStore).findById(invoiceId);

        verify(invoiceMapper).toResponse(invoice);

        assertSame(response, result);
    }

    @Test
    void shouldGetInvoiceByInvoiceNumber() {

        String invoiceNumber = "INV-2026-000001";

        Invoice invoice = createInvoice();

        InvoiceResponse response = InvoiceResponse.builder().build();

        when(invoiceStore.findByInvoiceNumber(invoiceNumber)).thenReturn(invoice);

        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        InvoiceResponse result = invoiceService.getByInvoiceNumber(invoiceNumber);

        verify(invoiceStore).findByInvoiceNumber(invoiceNumber);

        verify(invoiceMapper).toResponse(invoice);

        assertSame(response, result);
    }

    private Invoice createInvoice() {

        return Invoice.builder()
                .build();
    }

    private CreateInvoiceRequest createRequest() {

        CreateInvoiceItemRequest item = CreateInvoiceItemRequest.builder()
                .serviceId(1L)
                .quantity(BigDecimal.ONE)
                .discountRate(BigDecimal.ZERO)
                .build();

        return CreateInvoiceRequest.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(List.of(item))
                .build();
    }
}