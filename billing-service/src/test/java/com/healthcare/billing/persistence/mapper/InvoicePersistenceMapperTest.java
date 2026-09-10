package com.healthcare.billing.persistence.mapper;

import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.exception.InvoicePersistenceMappingException;
import com.healthcare.billing.model.entity.InvoiceItem;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceReconstitutor;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.persistence.entity.InvoiceItemJpaEntity;
import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoicePersistenceMapperTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final LocalDate ISSUED_DATE = LocalDate.of(2026, 9, 9);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 16);

    private InvoicePersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InvoicePersistenceMapper(new InvoiceReconstitutor());
    }

    @Test
    void shouldReturnNullForNullDomainEntity() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullForNullInvoice() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void shouldMapDraftEntityToDomain() {
        InvoiceJpaEntity entity = validDraftEntity();

        Invoice invoice = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(entity.getId(), invoice.getId()),
                () -> assertEquals(entity.getPatientId(), invoice.getPatientId()),
                () -> assertEquals(entity.getMedicalFacilityId(), invoice.getMedicalFacilityId()),
                () -> assertEquals(InvoiceStatus.DRAFT, invoice.getStatus()),
                () -> assertEquals(EUR, invoice.getTotalAmount().currency()),
                () -> assertEquals(1, invoice.getItems().size())
        );
    }

    @Test
    void shouldMapInvoiceToPersistenceEntity() {
        Invoice invoice = validDomainInvoice();

        InvoiceJpaEntity entity = mapper.toEntity(invoice);

        assertAll(
                () -> assertEquals("EUR", entity.getCurrency()),
                () -> assertEquals(InvoiceStatus.DRAFT, entity.getStatus()),
                () -> assertEquals(1, entity.getItems().size()),
                () -> assertSame(entity, entity.getItems().get(0).getInvoice())
        );
    }

    @Test
    void shouldUpdateOnlyLifecycleFields() {
        InvoiceJpaEntity entity = validDraftEntity();
        Invoice invoice = new InvoiceReconstitutor().restore(
                10L,
                "INV-2026-000010",
                1L,
                10L,
                List.of(validDomainItem()),
                money("100.00"),
                money("10.00"),
                money("17.10"),
                money("107.10"),
                InvoiceStatus.ISSUED,
                ISSUED_DATE,
                DUE_DATE
        );

        mapper.updateEntity(invoice, entity);

        assertAll(
                () -> assertEquals("INV-2026-000010", entity.getInvoiceNumber()),
                () -> assertEquals(InvoiceStatus.ISSUED, entity.getStatus()),
                () -> assertEquals(ISSUED_DATE, entity.getIssuedDate()),
                () -> assertEquals(DUE_DATE, entity.getDueDate()),
                () -> assertEquals(1L, entity.getPatientId()),
                () -> assertEquals(10L, entity.getMedicalFacilityId())
        );
    }

    @Test
    void shouldRejectNullPersistedCurrency() {
        InvoiceJpaEntity entity = validDraftEntity();
        entity.setCurrency(null);

        assertThrows(InvalidPersistedCurrencyException.class, () -> mapper.toDomain(entity));
    }

    @Test
    void shouldRejectBlankPersistedCurrency() {
        InvoiceJpaEntity entity = validDraftEntity();
        entity.setCurrency("   ");

        assertThrows(InvalidPersistedCurrencyException.class, () -> mapper.toDomain(entity));
    }

    @Test
    void shouldRejectUnknownPersistedCurrency() {
        InvoiceJpaEntity entity = validDraftEntity();
        entity.setCurrency("INVALID");

        assertThrows(InvalidPersistedCurrencyException.class, () -> mapper.toDomain(entity));
    }

    @Test
    void shouldStripPersistedCurrency() {
        InvoiceJpaEntity entity = validDraftEntity();
        entity.setCurrency(" EUR ");

        Invoice invoice = mapper.toDomain(entity);

        assertEquals(EUR, invoice.getTotalAmount().currency());
    }

    @Test
    void shouldRejectNullPersistenceItemsList() {
        InvoiceJpaEntity entity = validDraftEntity();
        entity.setItems(null);

        assertThrows(InvoicePersistenceMappingException.class, () -> mapper.toDomain(entity));
    }

    @Test
    void shouldRejectNullPersistenceItem() {
        InvoiceJpaEntity entity = validDraftEntity();
        List<InvoiceItemJpaEntity> items = new ArrayList<>();
        items.add(null);
        entity.setItems(items);

        assertThrows(InvoicePersistenceMappingException.class, () -> mapper.toDomain(entity));
    }

    @Test
    void shouldRejectNullDomainItemsList() {
        Invoice invoice = Invoice.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(null)
                .netAmount(money("100.00"))
                .discountAmount(money("10.00"))
                .taxAmount(money("17.10"))
                .totalAmount(money("107.10"))
                .build();

        assertThrows(InvoicePersistenceMappingException.class, () -> mapper.toEntity(invoice));
    }

    @Test
    void shouldRejectNullDomainItem() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(null);

        Invoice invoice = Invoice.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(items)
                .netAmount(money("100.00"))
                .discountAmount(money("10.00"))
                .taxAmount(money("17.10"))
                .totalAmount(money("107.10"))
                .build();

        assertThrows(InvoicePersistenceMappingException.class, () -> mapper.toEntity(invoice));
    }

    private InvoiceJpaEntity validDraftEntity() {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(1L);
        entity.setPatientId(1L);
        entity.setMedicalFacilityId(10L);
        entity.setCurrency("EUR");
        entity.setNetAmount(new BigDecimal("100.00"));
        entity.setDiscountAmount(new BigDecimal("10.00"));
        entity.setTaxAmount(new BigDecimal("17.10"));
        entity.setTotalAmount(new BigDecimal("107.10"));
        entity.setStatus(InvoiceStatus.DRAFT);
        entity.addItem(validPersistenceItem());
        return entity;
    }

    private InvoiceItemJpaEntity validPersistenceItem() {
        InvoiceItemJpaEntity item = new InvoiceItemJpaEntity();
        item.setId(1L);
        item.setServiceId(1L);
        item.setDescription("Consultation");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setDiscountRate(new BigDecimal("0.10"));
        item.setTaxRate(new BigDecimal("0.19"));
        item.setNetAmount(new BigDecimal("100.00"));
        item.setDiscountAmount(new BigDecimal("10.00"));
        item.setTaxAmount(new BigDecimal("17.10"));
        item.setTotalAmount(new BigDecimal("107.10"));
        return item;
    }

    private Invoice validDomainInvoice() {
        return Invoice.builder()
                .patientId(1L)
                .medicalFacilityId(10L)
                .items(List.of(validDomainItem()))
                .netAmount(money("100.00"))
                .discountAmount(money("10.00"))
                .taxAmount(money("17.10"))
                .totalAmount(money("107.10"))
                .build();
    }

    private InvoiceItem validDomainItem() {
        return InvoiceItem.builder()
                .serviceId(1L)
                .description("Consultation")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("100.00"))
                .discountRate(new BigDecimal("0.10"))
                .taxRate(new BigDecimal("0.19"))
                .netAmount(money("100.00"))
                .discountAmount(money("10.00"))
                .taxAmount(money("17.10"))
                .totalAmount(money("107.10"))
                .build();
    }

    private Money money(String amount) {
        return Money.of(new BigDecimal(amount), EUR);
    }
}
