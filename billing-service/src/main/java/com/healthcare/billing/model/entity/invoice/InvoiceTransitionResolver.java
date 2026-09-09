package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.exception.InvalidInvoiceTransitionException;
import com.healthcare.billing.exception.InvoiceStateMachineException;
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

        validateResolveParams(currentState, event);

        InvoiceStatus nextState = TRANSITIONS.get(new StateEventKey(currentState, event));

        if (nextState == null) {
            throw new InvalidInvoiceTransitionException(currentState, event);
        }

        return nextState;
    }

    private void validateResolveParams(InvoiceStatus currentState,
                                       InvoiceEvent event) {
        if (currentState == null) {
            throw new InvoiceStateMachineException("Current invoice state must not be null");
        }

        if (event == null) {
            throw new InvoiceStateMachineException("Invoice event must not be null");
        }
    }

    private record StateEventKey(
            InvoiceStatus state,
            InvoiceEvent event
    ) {
    }
}