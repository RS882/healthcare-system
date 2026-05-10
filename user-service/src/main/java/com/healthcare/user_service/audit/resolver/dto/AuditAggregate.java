package com.healthcare.user_service.audit.resolver.dto;

public record AuditAggregate(
        String aggregateType,
        String aggregateId) {
}
