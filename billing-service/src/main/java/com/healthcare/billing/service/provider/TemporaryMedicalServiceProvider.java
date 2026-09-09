package com.healthcare.billing.service.provider;

import com.healthcare.billing.exception.MedicalServiceNotFoundException;
import com.healthcare.billing.exception.MedicalServiceProviderException;
import com.healthcare.billing.model.service.MedicalServiceData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class TemporaryMedicalServiceProvider implements MedicalServiceProvider {

    private static final Currency EUR = Currency.getInstance("EUR");

    private static final Map<Long, MedicalServiceData> SERVICES =
            Map.ofEntries(

                    Map.entry(
                            1L,
                            new MedicalServiceData(
                                    1L,
                                    "General medical consultation",
                                    new BigDecimal("85.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            2L,
                            new MedicalServiceData(
                                    2L,
                                    "Specialist consultation",
                                    new BigDecimal("120.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            3L,
                            new MedicalServiceData(
                                    3L,
                                    "Blood test",
                                    new BigDecimal("35.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            4L,
                            new MedicalServiceData(
                                    4L,
                                    "Urine test",
                                    new BigDecimal("25.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            5L,
                            new MedicalServiceData(
                                    5L,
                                    "Ultrasound examination",
                                    new BigDecimal("95.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            6L,
                            new MedicalServiceData(
                                    6L,
                                    "X-ray examination",
                                    new BigDecimal("70.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            7L,
                            new MedicalServiceData(
                                    7L,
                                    "ECG examination",
                                    new BigDecimal("45.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            8L,
                            new MedicalServiceData(
                                    8L,
                                    "MRI examination",
                                    new BigDecimal("320.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            9L,
                            new MedicalServiceData(
                                    9L,
                                    "Physiotherapy session",
                                    new BigDecimal("65.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    ),

                    Map.entry(
                            10L,
                            new MedicalServiceData(
                                    10L,
                                    "Vaccination service",
                                    new BigDecimal("40.00"),
                                    new BigDecimal("0.19"),
                                    EUR
                            )
                    )
            );

    @Override
    public MedicalServiceData getById(Long serviceId) {

        if (serviceId == null || serviceId <= 0) {
            throw new MedicalServiceProviderException("Medical service id must be greater than zero");
        }

        MedicalServiceData service = SERVICES.get(serviceId);

        if (service == null) {
            throw new MedicalServiceNotFoundException(serviceId);
        }

        return service;
    }

    @Override
    public List<MedicalServiceData> getByIdList(List<Long> serviceIds) {

        if (serviceIds == null) {
            throw new MedicalServiceProviderException("Medical service id list must not be null");
        }

        if (serviceIds.stream().anyMatch(Objects::isNull)) {
            throw new MedicalServiceProviderException("Medical service id list must not contain null");
        }

        if (serviceIds.isEmpty()) {
            return List.of();
        }

        return serviceIds.stream()
                .map(SERVICES::get)
                .filter(Objects::nonNull)
                .toList();
    }
}