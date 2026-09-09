package com.healthcare.billing.service;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.InvoiceCreationException;
import com.healthcare.billing.exception.MedicalServiceNotFoundException;
import com.healthcare.billing.exception.MedicalServiceProviderException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.service.MedicalServiceData;
import com.healthcare.billing.money.MoneyPolicy;
import com.healthcare.billing.service.provider.MedicalServiceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InvoiceCreatorTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    private MedicalServiceProvider medicalServiceProvider;
    private InvoiceCalculator invoiceCalculator;
    private InvoiceItemCalculator invoiceItemCalculator;
    private MoneyPolicy moneyPolicy;
    private InvoiceCreator invoiceCreator;

    @BeforeEach
    void setUp() {
        medicalServiceProvider = mock(MedicalServiceProvider.class);
        invoiceCalculator = mock(InvoiceCalculator.class);
        invoiceItemCalculator = mock(InvoiceItemCalculator.class);
        moneyPolicy = mock(MoneyPolicy.class);

        when(moneyPolicy.currency()).thenReturn(EUR);

        invoiceCreator = new InvoiceCreator(
                medicalServiceProvider,
                invoiceCalculator,
                invoiceItemCalculator,
                moneyPolicy
        );
    }

    @Test
    void shouldCreateDraftInvoice() {
        CreateInvoiceItemRequest firstRequest = itemRequest(1L, "2", "0.10");
        CreateInvoiceItemRequest secondRequest = itemRequest(2L, "1", "0.00");
        CreateInvoiceRequest request = request(List.of(firstRequest, secondRequest));

        MedicalServiceData firstService = service(1L, EUR);
        MedicalServiceData secondService = service(2L, EUR);
        when(medicalServiceProvider.getByIdList(List.of(1L, 2L)))
                .thenReturn(List.of(firstService, secondService));

        InvoiceItem firstItem = InvoiceItem.builder().build();
        InvoiceItem secondItem = InvoiceItem.builder().build();

        when(invoiceItemCalculator.calculate(
                1L,
                firstService.description(),
                firstRequest.quantity(),
                firstService.unitPrice(),
                firstRequest.discountRate(),
                firstService.taxRate()
        )).thenReturn(firstItem);

        when(invoiceItemCalculator.calculate(
                2L,
                secondService.description(),
                secondRequest.quantity(),
                secondService.unitPrice(),
                secondRequest.discountRate(),
                secondService.taxRate()
        )).thenReturn(secondItem);

        Invoice expected = Invoice.builder().build();
        when(invoiceCalculator.calculate(1L, 10L, List.of(firstItem, secondItem)))
                .thenReturn(expected);

        Invoice result = invoiceCreator.createDraft(request);

        assertSame(expected, result);
        verify(medicalServiceProvider).getByIdList(List.of(1L, 2L));
        verify(invoiceCalculator).calculate(1L, 10L, List.of(firstItem, secondItem));
    }

    @Test
    void shouldRequestDistinctMedicalServiceIds() {
        CreateInvoiceRequest request = request(List.of(
                itemRequest(1L, "1", "0"),
                itemRequest(1L, "2", "0.10")
        ));

        MedicalServiceData service = service(1L, EUR);
        when(medicalServiceProvider.getByIdList(List.of(1L)))
                .thenReturn(List.of(service));
        when(invoiceItemCalculator.calculate(anyLong(), anyString(), any(), any(), any(), any()))
                .thenReturn(InvoiceItem.builder().build());
        when(invoiceCalculator.calculate(anyLong(), anyLong(), anyList()))
                .thenReturn(Invoice.builder().build());

        invoiceCreator.createDraft(request);

        verify(medicalServiceProvider).getByIdList(List.of(1L));
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(InvoiceCreationException.class, () -> invoiceCreator.createDraft(null));
        verifyNoInteractions(medicalServiceProvider, invoiceCalculator, invoiceItemCalculator);
    }

    @Test
    void shouldRejectNullItems() {
        assertThrows(
                InvoiceCreationException.class,
                () -> invoiceCreator.createDraft(request(null))
        );
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(
                InvoiceCreationException.class,
                () -> invoiceCreator.createDraft(request(List.of()))
        );
    }

    @Test
    void shouldRejectItemsContainingNull() {
        List<CreateInvoiceItemRequest> items = new ArrayList<>();
        items.add(itemRequest(1L, "1", "0"));
        items.add(null);

        assertThrows(
                InvoiceCreationException.class,
                () -> invoiceCreator.createDraft(request(items))
        );
    }

    @Test
    void shouldRejectNullProviderResult() {
        CreateInvoiceRequest request = request(List.of(itemRequest(1L, "1", "0")));
        when(medicalServiceProvider.getByIdList(List.of(1L))).thenReturn(null);

        assertThrows(
                MedicalServiceProviderException.class,
                () -> invoiceCreator.createDraft(request)
        );
    }

    @Test
    void shouldRejectProviderResultContainingNull() {
        CreateInvoiceRequest request = request(List.of(itemRequest(1L, "1", "0")));
        List<MedicalServiceData> services = new ArrayList<>();
        services.add(null);
        when(medicalServiceProvider.getByIdList(List.of(1L))).thenReturn(services);

        assertThrows(
                MedicalServiceProviderException.class,
                () -> invoiceCreator.createDraft(request)
        );
    }

    @Test
    void shouldReportAllMissingMedicalServices() {
        CreateInvoiceRequest request = request(List.of(
                itemRequest(1L, "1", "0"),
                itemRequest(2L, "1", "0"),
                itemRequest(3L, "1", "0")
        ));
        when(medicalServiceProvider.getByIdList(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(service(1L, EUR)));

        assertThrows(
                MedicalServiceNotFoundException.class,
                () -> invoiceCreator.createDraft(request)
        );
    }

    @Test
    void shouldRejectNullMedicalServiceCurrency() {
        CreateInvoiceRequest request = request(List.of(itemRequest(1L, "1", "0")));
        when(medicalServiceProvider.getByIdList(List.of(1L)))
                .thenReturn(List.of(service(1L, null)));

        assertThrows(
                InvoiceCreationException.class,
                () -> invoiceCreator.createDraft(request)
        );
    }

    @Test
    void shouldRejectCurrencyMismatch() {
        CreateInvoiceRequest request = request(List.of(itemRequest(1L, "1", "0")));
        when(medicalServiceProvider.getByIdList(List.of(1L)))
                .thenReturn(List.of(service(1L, USD)));

        assertThrows(
                CurrencyMismatchException.class,
                () -> invoiceCreator.createDraft(request)
        );
    }

    private CreateInvoiceRequest request(List<CreateInvoiceItemRequest> items) {
        return CreateInvoiceRequest.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(items)
                .build();
    }

    private CreateInvoiceItemRequest itemRequest(
            Long serviceId,
            String quantity,
            String discountRate
    ) {
        return CreateInvoiceItemRequest.builder()
                .serviceId(serviceId)
                .quantity(new BigDecimal(quantity))
                .discountRate(new BigDecimal(discountRate))
                .build();
    }

    private MedicalServiceData service(Long id, Currency currency) {
        return new MedicalServiceData(
                id,
                "Service " + id,
                new BigDecimal("100.00"),
                new BigDecimal("0.19"),
                currency
        );
    }
}
