package com.healthcare.billing.persistence.mapper;


import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.exception.InvoicePersistenceMappingException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceReconstitutor;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.persistence.entity.InvoiceItemJpaEntity;
import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Currency;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoicePersistenceMapper {

    private final InvoiceReconstitutor invoiceReconstitutor;

    public Invoice toDomain(InvoiceJpaEntity entity) {

        if (entity == null) {
            return null;
        }

        Currency currency = resolveCurrency(entity.getCurrency());

        List<InvoiceItem> items = toDomainItems(entity.getItems(), currency);

        return invoiceReconstitutor.restore(
                entity.getId(),
                entity.getInvoiceNumber(),
                entity.getPatientId(),
                entity.getMedicalFacilityId(),
                items,
                Money.of(entity.getNetAmount(), currency),
                Money.of(entity.getDiscountAmount(), currency),
                Money.of(entity.getTaxAmount(), currency),
                Money.of(entity.getTotalAmount(), currency),
                entity.getStatus(),
                entity.getIssuedDate(),
                entity.getDueDate()
        );
    }

    public InvoiceJpaEntity toEntity(Invoice invoice) {

        if (invoice == null) {
            return null;
        }

        InvoiceJpaEntity entity = new InvoiceJpaEntity();

        entity.setId(invoice.getId());

        entity.setInvoiceNumber(invoice.getInvoiceNumber());
        entity.setPatientId(invoice.getPatientId());
        entity.setMedicalFacilityId(invoice.getMedicalFacilityId());

        entity.setCurrency(
                invoice.getTotalAmount()
                        .currency()
                        .getCurrencyCode()
        );

        entity.setNetAmount(invoice.getNetAmount().amount());

        entity.setDiscountAmount(invoice.getDiscountAmount().amount());

        entity.setTaxAmount(invoice.getTaxAmount().amount());

        entity.setTotalAmount(invoice.getTotalAmount().amount());

        entity.setStatus(invoice.getStatus());
        entity.setIssuedDate(invoice.getIssuedDate());
        entity.setDueDate(invoice.getDueDate());

        if (invoice.getItems() == null) {
            throw new InvoicePersistenceMappingException("Invoice items must not be null");
        }

        invoice.getItems()
                .stream()
                .map(this::toEntityItem)
                .forEach(entity::addItem);

        return entity;
    }

    public void updateEntity(
            Invoice invoice,
            InvoiceJpaEntity entity
    ) {

        entity.setInvoiceNumber(invoice.getInvoiceNumber());

        entity.setStatus(invoice.getStatus());

        entity.setIssuedDate(invoice.getIssuedDate());

        entity.setDueDate(invoice.getDueDate());
    }

    private List<InvoiceItem> toDomainItems(
            List<InvoiceItemJpaEntity> entities,
            Currency currency
    ) {

        if (entities == null) {
            throw new InvoicePersistenceMappingException("Invoice item entities must not be null");
        }

        return entities.stream()
                .map(entity -> toDomainItem(entity, currency))
                .toList();
    }

    private InvoiceItem toDomainItem(
            InvoiceItemJpaEntity entity,
            Currency currency
    ) {

        if (entity == null) {
            throw new InvoicePersistenceMappingException("Invoice item persistence entity must not be null");
        }

        return InvoiceItem.builder()
                .id(entity.getId())
                .serviceId(entity.getServiceId())
                .description(entity.getDescription())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .discountRate(entity.getDiscountRate())
                .taxRate(entity.getTaxRate())
                .netAmount(Money.of(entity.getNetAmount(), currency))
                .discountAmount(Money.of(entity.getDiscountAmount(), currency))
                .taxAmount(Money.of(entity.getTaxAmount(), currency))
                .totalAmount(Money.of(entity.getTotalAmount(), currency))
                .build();
    }

    private InvoiceItemJpaEntity toEntityItem(InvoiceItem item) {

        if (item == null) {
            throw new InvoicePersistenceMappingException("Invoice item must not be null");
        }

        InvoiceItemJpaEntity entity = new InvoiceItemJpaEntity();

        entity.setId(item.getId());
        entity.setServiceId(item.getServiceId());
        entity.setDescription(item.getDescription());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setDiscountRate(item.getDiscountRate());
        entity.setTaxRate(item.getTaxRate());

        entity.setNetAmount(item.getNetAmount().amount());
        entity.setDiscountAmount(item.getDiscountAmount().amount());
        entity.setTaxAmount(item.getTaxAmount().amount());
        entity.setTotalAmount(item.getTotalAmount().amount());

        return entity;
    }

    private Currency resolveCurrency(String currencyCode) {

        if (!StringUtils.hasText(currencyCode)) {
            throw new InvalidPersistedCurrencyException(currencyCode);
        }

        try {
            return Currency.getInstance(currencyCode.strip());
        } catch (IllegalArgumentException exception) {
            throw new InvalidPersistedCurrencyException(currencyCode);
        }
    }
}
