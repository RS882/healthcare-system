package com.healthcare.billing.service;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.service.MedicalServiceData;
import com.healthcare.billing.service.provider.MedicalServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoiceCreator {

    private final MedicalServiceProvider medicalServiceProvider;
    private final InvoiceCalculator invoiceCalculator;
    private final InvoiceItemCalculator invoiceItemCalculator;
    private final BillingProperties properties;

    public Invoice createDraft(CreateInvoiceRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Create invoice request must not be null");
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

        Map<Long, MedicalServiceData> services = medicalServiceProvider.getByIdList(serviceIds)
                .stream()
                .collect(Collectors.toMap(
                        MedicalServiceData::id,
                        Function.identity()
                ));

        List<Long> missingServiceIds = serviceIds.stream()
                .filter(serviceId -> !services.containsKey(serviceId))
                .toList();

        if (!missingServiceIds.isEmpty()) {
            throw new IllegalArgumentException("Medical services not found: %s".formatted(missingServiceIds));
        }

        return services;
    }

    private void validateCurrency(Currency serviceCurrency) {

        if (serviceCurrency == null) {
            throw new IllegalArgumentException("Item currency must not be null");
        }

        Currency invoiceCurrency = Currency.getInstance(properties.currency());

        if (!serviceCurrency.equals(invoiceCurrency)) {
            throw new IllegalArgumentException(
                    "Medical service currency %s does not match invoice currency %s"
                            .formatted(
                                    serviceCurrency.getCurrencyCode(),
                                    invoiceCurrency.getCurrencyCode()
                            )
            );
        }
    }
}
