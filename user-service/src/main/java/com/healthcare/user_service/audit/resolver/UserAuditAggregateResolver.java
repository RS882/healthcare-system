package com.healthcare.user_service.audit.resolver;

import com.healthcare.user_service.audit.resolver.dto.AuditAggregate;
import com.healthcare.user_service.audit.resolver.interfacies.AuditAggregateResolver;
import com.healthcare.user_service.kafka.event.DomainEvent;
import com.healthcare.user_service.kafka.event.UserEvent;
import org.springframework.stereotype.Component;

import static com.healthcare.user_service.outbox.constant.AggregateType.AGGREGATE_TYPE_USER;

@Component
public class UserAuditAggregateResolver implements AuditAggregateResolver {

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof UserEvent;
    }

    @Override
    public AuditAggregate resolve(DomainEvent event) {
        UserEvent userEvent = (UserEvent) event;

        return new AuditAggregate(
                AGGREGATE_TYPE_USER.value(),
                String.valueOf(userEvent.userId())
        );
    }
}
