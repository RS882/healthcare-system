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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoiceCreator {

    private final MedicalServiceProvider medicalServiceProvider;
    private final InvoiceCalculator invoiceCalculator;
    private final InvoiceItemCalculator invoiceItemCalculator;
    private final MoneyPolicy moneyPolicy;

    public Invoice createDraft(CreateInvoiceRequest request) {

        if (request == null) {
            throw new InvoiceCreationException("Create invoice request must not be null");
        }

        return invoiceCalculator.calculate(
                request.patientId(),
                request.medicalFacilityId(),
                createInvoiceItems(request.items())
        );
    }

    private List<InvoiceItem> createInvoiceItems(
            List<CreateInvoiceItemRequest> items
    ) {

        validateItems(items);

        Map<Long, MedicalServiceData> services = resolveMedicalServiceData(items);

        return items.stream()
                .map(item -> createInvoiceItem(
                        item,
                        services.get(item.serviceId())
                ))
                .toList();
    }

    private InvoiceItem createInvoiceItem(
            CreateInvoiceItemRequest request,
            MedicalServiceData serviceData) {

        validateCurrency(serviceData.currency());

        return invoiceItemCalculator.calculate(
                serviceData.id(),
                serviceData.description(),
                request.quantity(),
                serviceData.unitPrice(),
                request.discountRate(),
                serviceData.taxRate()
        );
    }

    private Map<Long, MedicalServiceData> resolveMedicalServiceData(
            List<CreateInvoiceItemRequest> items
    ) {
        List<Long> serviceIds = items.stream()
                .map(CreateInvoiceItemRequest::serviceId)
                .distinct()
                .toList();

        List<MedicalServiceData> serviceDataList = medicalServiceProvider.getByIdList(serviceIds);

        validateMedicalServiceDataList(serviceDataList);

        Map<Long, MedicalServiceData> services = serviceDataList.stream()
                .collect(Collectors.toMap(
                        MedicalServiceData::id,
                        Function.identity()
                ));

        checkMissingServiceIds(serviceIds, services);

        return services;
    }

    private void checkMissingServiceIds(List<Long> serviceIds, Map<Long, MedicalServiceData> services){
        List<Long> missingServiceIds = serviceIds.stream()
                .filter(serviceId -> !services.containsKey(serviceId))
                .toList();

        if (!missingServiceIds.isEmpty()) {
            throw new MedicalServiceNotFoundException(missingServiceIds);
        }
    }

    private void validateItems(List<CreateInvoiceItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new InvoiceCreationException("Invoice items must not be null or empty");
        }

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new InvoiceCreationException("Invoice items must not contain null");
        }
    }

    private void validateCurrency(Currency serviceCurrency) {

        if (serviceCurrency == null) {
            throw new InvoiceCreationException("Medical service currency must not be null");
        }

        Currency invoiceCurrency = moneyPolicy.currency();

        if (!serviceCurrency.equals(invoiceCurrency)) {
            throw new CurrencyMismatchException(invoiceCurrency, serviceCurrency);
        }
    }

    private void validateMedicalServiceDataList(List<MedicalServiceData> serviceDataList){

        if (serviceDataList == null) {
            throw new MedicalServiceProviderException("Medical service provider returned null");
        }

        if (serviceDataList.stream().anyMatch(Objects::isNull)) {
            throw new MedicalServiceProviderException("Medical service provider returned null service data");
        }
    }
}
