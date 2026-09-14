package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Payment {

    private Long id;
    private Long invoiceId;
    private Money amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    @Builder
    public Payment(
            Long invoiceId,
            Money amount,
            PaymentMethod method,
            Instant createdAt
    ) {
        validateParams(invoiceId, amount, method);

        validateCreatedAt(createdAt);

        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;

        this.createdAt = createdAt;
        this.status = PaymentStatus.CREATED;
    }

    void applyStatus(
            PaymentStatus status,
            Instant eventTime
    ) {

        validateStatus(status);
        validateEventTime(eventTime);

        this.status = status;
        this.updatedAt = eventTime;
        if (status == PaymentStatus.COMPLETED) {
            this.completedAt = eventTime;
        }
    }

    public void start(
            Instant eventTime,
            PaymentStateResolver resolver
    ) {

        validateResolver(resolver);

        resolver.resolve(this.status)
                .start(this, eventTime);
    }

    public void complete(
            Instant eventTime,
            PaymentStateResolver resolver
    ) {

        validateResolver(resolver);

        resolver.resolve(this.status)
                .complete(this, eventTime);
    }


    public void cancel(
            Instant eventTime,
            PaymentStateResolver resolver
    ) {

        validateResolver(resolver);

        resolver.resolve(this.status)
                .cancel(this, eventTime);
    }

    private void validateResolver(PaymentStateResolver resolver) {
        if (resolver == null) {
            throw new PaymentValidationException("Resolver cannot be null");
        }
    }

    private void validateParams(
            Long invoiceId,
            Money amount,
            PaymentMethod method
    ) {

        if (invoiceId == null) {
            throw new PaymentValidationException("Invoice id cannot be null");
        }

        if (invoiceId <= 0) {
            throw new PaymentValidationException("Invoice id must be greater than zero");
        }

        if (amount == null) {
            throw new PaymentValidationException("Amount cannot be null");
        }

        if (amount.isZero()) {
            throw new PaymentValidationException("Amount cannot be zero");
        }

        if (amount.isNegative()) {
            throw new PaymentValidationException("Amount cannot be negative");
        }

        if (method == null) {
            throw new PaymentValidationException("Method cannot be null");
        }
    }

    private void validateCreatedAt(Instant createdAt) {

        if (createdAt == null) {
            throw new PaymentValidationException("Created at cannot be null");
        }
    }

    private void validateEventTime(Instant eventTime) {
        if (eventTime == null) {
            throw new PaymentValidationException("Event time cannot be null");
        }
        if (eventTime.isBefore(createdAt)) {
            throw new PaymentValidationException("Event time cannot be before created at");
        }

        if (updatedAt != null && eventTime.isBefore(updatedAt)) {
            throw new PaymentValidationException("Event time cannot be before updated at");
        }
    }

    private void validateStatus(PaymentStatus status) {
        if (status == null) {
            throw new PaymentValidationException("Payment status must not be null");
        }
    }
}
