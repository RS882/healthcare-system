package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class InvoiceTransitionResolver {

    private static final Map<StateEventKey, InvoiceStatus> TRANSITIONS =
            Map.of(
                    new StateEventKey(
                            InvoiceStatus.DRAFT,
                            InvoiceEvent.ISSUE
                    ),
                    InvoiceStatus.ISSUED,

                    new StateEventKey(
                            InvoiceStatus.DRAFT,
                            InvoiceEvent.CANCEL
                    ),
                    InvoiceStatus.CANCELLED,

                    new StateEventKey(
                            InvoiceStatus.ISSUED,
                            InvoiceEvent.PAY
                    ),
                    InvoiceStatus.PAID,

                    new StateEventKey(
                            InvoiceStatus.ISSUED,
                            InvoiceEvent.CANCEL
                    ),
                    InvoiceStatus.CANCELLED
            );

    public InvoiceStatus resolve(
            InvoiceStatus currentState,
            InvoiceEvent event
    ) {
        InvoiceStatus nextState = TRANSITIONS.get(
                new StateEventKey(currentState, event)
        );

        if (nextState == null) {
            throw new IllegalStateException(
                    "Transition from %s by event %s is not allowed"
                            .formatted(
                                    currentState,
                                    event
                            )
            );
        }

        return nextState;
    }

    private record StateEventKey(
            InvoiceStatus state,
            InvoiceEvent event
    ) {
    }
}