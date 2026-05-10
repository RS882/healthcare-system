package com.healthcare.user_service.audit.resolver.interfacies;

import com.healthcare.user_service.audit.resolver.dto.AuditAggregate;
import com.healthcare.user_service.kafka.event.DomainEvent;

public interface AuditAggregateResolver {

    boolean supports(DomainEvent event);

    AuditAggregate resolve(DomainEvent event);
}
