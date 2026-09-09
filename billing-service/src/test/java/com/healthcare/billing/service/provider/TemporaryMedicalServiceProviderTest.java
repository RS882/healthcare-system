package com.healthcare.billing.service.provider;

import com.healthcare.billing.exception.MedicalServiceNotFoundException;
import com.healthcare.billing.exception.MedicalServiceProviderException;
import com.healthcare.billing.model.service.MedicalServiceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporaryMedicalServiceProviderTest {

    private TemporaryMedicalServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TemporaryMedicalServiceProvider();
    }

    @Test
    void shouldReturnServiceById() {
        MedicalServiceData result = provider.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("EUR", result.currency().getCurrencyCode());
    }

    @Test
    void shouldRejectInvalidSingleId() {
        assertThrows(MedicalServiceProviderException.class, () -> provider.getById(null));
        assertThrows(MedicalServiceProviderException.class, () -> provider.getById(0L));
        assertThrows(MedicalServiceProviderException.class, () -> provider.getById(-1L));
    }

    @Test
    void shouldThrowNotFoundForUnknownSingleId() {
        assertThrows(MedicalServiceNotFoundException.class, () -> provider.getById(999L));
    }

    @Test
    void shouldReturnServicesByIds() {
        List<MedicalServiceData> result = provider.getByIdList(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertEquals(List.of(1L, 2L), result.stream().map(MedicalServiceData::id).toList());
    }

    @Test
    void shouldReturnEmptyListForEmptyIds() {
        assertTrue(provider.getByIdList(List.of()).isEmpty());
    }

    @Test
    void shouldRejectNullIdList() {
        assertThrows(MedicalServiceProviderException.class, () -> provider.getByIdList(null));
    }

    @Test
    void shouldRejectIdListContainingNull() {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(null);

        assertThrows(MedicalServiceProviderException.class, () -> provider.getByIdList(ids));
    }

    @Test
    void shouldOmitUnknownIdsInList() {
        List<MedicalServiceData> result = provider.getByIdList(List.of(1L, 999L));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }
}
