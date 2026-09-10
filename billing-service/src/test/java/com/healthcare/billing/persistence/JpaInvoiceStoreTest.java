package com.healthcare.billing.persistence;

import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import com.healthcare.billing.persistence.mapper.InvoicePersistenceMapper;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JpaInvoiceStoreTest {

    private InvoiceJpaRepository repository;
    private InvoicePersistenceMapper mapper;
    private JpaInvoiceStore store;

    @BeforeEach
    void setUp() {
        repository = mock(InvoiceJpaRepository.class);
        mapper = mock(InvoicePersistenceMapper.class);
        store = new JpaInvoiceStore(repository, mapper);
    }

    @Test
    void shouldFindInvoiceById() {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        Invoice invoice = Invoice.builder().build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(invoice);

        assertSame(invoice, store.findById(1L));
        verify(mapper).toDomain(entity);
    }

    @Test
    void shouldThrowWhenInvoiceByIdNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> store.findById(1L));
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldFindInvoiceByNumber() {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        Invoice invoice = Invoice.builder().build();
        when(repository.findByInvoiceNumber("INV-2026-000001"))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(invoice);

        assertSame(invoice, store.findByInvoiceNumber("INV-2026-000001"));
    }

    @Test
    void shouldThrowWhenInvoiceByNumberNotFound() {
        when(repository.findByInvoiceNumber("INV-2026-999999"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvoiceNotFoundException.class,
                () -> store.findByInvoiceNumber("INV-2026-999999")
        );
    }

    @Test
    void shouldSaveInvoice() {
        Invoice invoice = Invoice.builder().build();
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        InvoiceJpaEntity savedEntity = new InvoiceJpaEntity();
        Invoice savedInvoice = Invoice.builder().build();

        when(mapper.toEntity(invoice)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedInvoice);

        assertSame(savedInvoice, store.save(invoice));
        verify(repository).save(entity);
    }

    @Test
    void shouldUpdateExistingInvoice() {
        Invoice invoice = mock(Invoice.class);
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        when(invoice.getId()).thenReturn(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        store.update(invoice);

        verify(mapper).updateEntity(invoice, entity);
    }

    @Test
    void shouldThrowWhenUpdatingMissingInvoice() {
        Invoice invoice = mock(Invoice.class);
        when(invoice.getId()).thenReturn(1L);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> store.update(invoice));
        verify(mapper, never()).updateEntity(any(), any());
    }
}
