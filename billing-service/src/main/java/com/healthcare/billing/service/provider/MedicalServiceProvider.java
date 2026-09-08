package com.healthcare.billing.service.provider;

import com.healthcare.billing.model.service.MedicalServiceData;

import java.util.List;

public interface MedicalServiceProvider {

    MedicalServiceData getById(Long serviceId);

    List<MedicalServiceData> getByIdList(List<Long> serviceIds);
}
