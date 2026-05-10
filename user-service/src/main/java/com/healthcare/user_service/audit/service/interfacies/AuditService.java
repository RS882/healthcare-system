package com.healthcare.user_service.audit.service.interfacies;

import com.healthcare.user_service.kafka.event.DomainEvent;

public interface AuditService {

    void recordEvent(DomainEvent event);
}
