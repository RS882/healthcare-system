package com.healthcare.user_service.audit.resolver;

import com.healthcare.user_service.audit.resolver.dto.AuditAggregate;
import com.healthcare.user_service.audit.resolver.interfacies.AuditAggregateResolver;
import com.healthcare.user_service.kafka.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.healthcare.user_service.outbox.constant.AggregateType.UNKNOWN_AGGREGATE_TYPE;

@Component
@RequiredArgsConstructor
public class CompositeAuditAggregateResolver {

    private final List<AuditAggregateResolver> resolvers;

    public AuditAggregate resolve(DomainEvent event) {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(event))
                .findFirst()
                .map(resolver -> resolver.resolve(event))
                .orElse(new AuditAggregate(UNKNOWN_AGGREGATE_TYPE.value(), null));
    }
}