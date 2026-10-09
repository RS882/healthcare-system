package com.healthcare.billing.persistence;

import com.healthcare.billing.exception.InvoiceNotFoundException;
import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.persistence.entity.InvoiceJpaEntity;
import com.healthcare.billing.persistence.mapper.InvoicePersistenceMapper;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class JpaInvoiceStoreTest {

    @Mock
    private InvoiceJpaRepository repository;

    @Mock
    private InvoicePersistenceMapper mapper;

    @InjectMocks
    private JpaInvoiceStore store;

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

    @ParameterizedTest(name = "Test {index}: invoice id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0,
            -172
    })
    void should_return_exception_when_invoice_id_is_negative_or_zero_or_null_upon_find_by_id(Long id) {

        assertThrows(InvoiceValidationException.class,
                () -> store.findById(id));

        verifyNoInteractions(mapper, repository);

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

    @ParameterizedTest(name = "Test {index}: invoice number [{arguments}]")
    @NullSource
    @ValueSource(strings = {
            "",
            "    "
    })
    void should_return_exception_when_invoice_number_is_null_or_empty(String invoiceNumber) {

        assertThrows(InvoiceValidationException.class,
                () -> store.findByInvoiceNumber(invoiceNumber));

        verifyNoInteractions(mapper, repository);

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
    void should_return_exception_when_invoice_is_null_upon_save() {

        assertThrows(InvoiceValidationException.class,
                () -> store.save(null));

        verifyNoInteractions(mapper, repository);
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

    @Test
    void should_return_exception_when_invoice_is_null_upon_update() {

        assertThrows(InvoiceValidationException.class,
                () -> store.update(null));

        verifyNoInteractions(mapper, repository);
    }

    @ParameterizedTest(name = "Test {index}: invoice id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0,
            -172
    })
    void should_return_exception_when_invoice_id_is_negative_or_zero_or_null_upon_update(Long id) {

        Invoice invoice = mock(Invoice.class);
        when(invoice.getId()).thenReturn(id);

        assertThrows(InvoiceValidationException.class,
                () -> store.update(invoice));

        verifyNoInteractions(mapper, repository);

    }

    @Test
    void should_return_true_when_invoice_is_existing() {
        Long id = 3L;

        when(repository.existsById(id)).thenReturn(true);

        assertTrue(store.existsById(id));

        verifyNoInteractions(mapper);

        verify(repository).existsById(id);
    }

    @Test
    void should_return_false_when_invoice_isnt_existing() {
        Long id = 3L;

        when(repository.existsById(id)).thenReturn(false);

        assertFalse(store.existsById(id));

        verifyNoInteractions(mapper);

        verify(repository).existsById(id);
    }

    @ParameterizedTest(name = "Test {index}: invoice id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0,
            -172
    })
    void should_return_exception_when_invoice_id_is_negative_or_zero_or_null_upon_exist_by_id(Long id) {

        assertThrows(InvoiceValidationException.class,
                () -> store.existsById(id));

        verifyNoInteractions(mapper, repository);

    }
}
