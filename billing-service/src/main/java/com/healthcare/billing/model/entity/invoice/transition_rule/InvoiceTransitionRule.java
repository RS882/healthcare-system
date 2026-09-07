package com.healthcare.billing.model.entity.invoice.transition_rule;

import com.healthcare.billing.model.entity.invoice.Invoice;

import java.util.function.Consumer;

public record InvoiceTransitionRule(
        Consumer<Invoice> guard,
        Consumer<Invoice> action
) {
    public static InvoiceTransitionRule empty() {
        return new InvoiceTransitionRule(
                invoice -> {
                },
                invoice -> {
                }
        );
    }
}