package com.healthcare.billing.payment.model;

import com.healthcare.billing.exception.PaymentReconstitutionException;
import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
        validateCreateParams(invoiceId, amount, method);
        verifyEventTimeExists(createdAt);

        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;

        this.createdAt = createdAt;
        this.status = PaymentStatus.CREATED;
    }

    private Payment(
            Long id,
            Long invoiceId,
            Money amount,
            PaymentMethod method,
            PaymentStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
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

    public static Payment reconstitute(
            Long id,
            Long invoiceId,
            Money amount,
            PaymentMethod method,
            PaymentStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {
        validateReconstituteParams(
                id,
                invoiceId,
                amount,
                method,
                status,
                createdAt,
                updatedAt,
                completedAt);

        return new Payment(
                id,
                invoiceId,
                amount,
                method,
                status,
                createdAt,
                updatedAt,
                completedAt
        );
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

    private static void validateReconstituteParams(
            Long id,
            Long invoiceId,
            Money amount,
            PaymentMethod method,
            PaymentStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {

        if (id == null) {
            throw new PaymentReconstitutionException("Id cannot be null");
        }

        if (id <= 0) {
            throw new PaymentReconstitutionException("Id must be greater than zero");
        }

        if (invoiceId == null) {
            throw new PaymentReconstitutionException("Invoice id cannot be null");
        }

        if (invoiceId <= 0) {
            throw new PaymentReconstitutionException("Invoice id must be greater than zero");
        }

        if (amount == null) {
            throw new PaymentReconstitutionException("Amount cannot be null");
        }

        if (amount.isZero()) {
            throw new PaymentReconstitutionException("Amount cannot be zero");
        }

        if (amount.isNegative()) {
            throw new PaymentReconstitutionException("Amount cannot be negative");
        }

        if (method == null) {
            throw new PaymentReconstitutionException("Method cannot be null");
        }

        validateTimestampsForStatus(
                status,
                createdAt,
                updatedAt,
                completedAt
        );
    }

    private static void validateTimestampsForStatus(
            PaymentStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {
        if (status == null) {
            throw new PaymentReconstitutionException("Payment status must not be null");
        }

        if (createdAt == null) {
            throw new PaymentReconstitutionException("Created at cannot be null");
        }

        List<String> exceptionMessages = new ArrayList<>();

        switch (status) {

            case CREATED -> {
                if (updatedAt != null) {
                    exceptionMessages.add("Updated at must be null");
                }
                if (completedAt != null) {
                    exceptionMessages.add("Completed at must be null");
                }
            }

            case PENDING, CANCELLED -> {
                if (updatedAt == null) {
                    exceptionMessages.add("Updated at cannot be null");
                }
                if (updatedAt != null && updatedAt.isBefore(createdAt)) {
                    exceptionMessages.add("Updated at cannot be before created at");
                }
                if (completedAt != null) {
                    exceptionMessages.add("Completed at must be null");
                }
            }

            case COMPLETED -> {
                if (updatedAt == null) {
                    exceptionMessages.add("Updated at cannot be null");
                }
                if (completedAt == null) {
                    exceptionMessages.add("Completed at cannot be null");
                }
                if (updatedAt != null && updatedAt.isBefore(createdAt)) {
                    exceptionMessages.add("Updated at cannot be before created at");
                }
                if (completedAt != null && completedAt.isBefore(createdAt)) {
                    exceptionMessages.add("Completed at cannot be before created at");
                }
                if (updatedAt != null && completedAt != null && !updatedAt.equals(completedAt)) {
                    exceptionMessages.add("Updated at and completed at must be equal");
                }
            }
        }

        if (!exceptionMessages.isEmpty()) {
            throw new PaymentReconstitutionException(exceptionMessages);
        }
    }

    private void validateCreateParams(
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

    private void validateEventTime(Instant eventTime) {
        verifyEventTimeExists(eventTime);
        verifyEventTimeOrdering(eventTime);
    }

    private void validateStatus(PaymentStatus status) {
        if (status == null) {
            throw new PaymentValidationException("Payment status must not be null");
        }
    }

    private void verifyEventTimeExists(Instant eventTime) {
        if (eventTime == null) {
            throw new PaymentValidationException("Event time cannot be null");
        }
    }

    private void verifyEventTimeOrdering(Instant eventTime) {

        if (eventTime.isBefore(createdAt)) {
            throw new PaymentValidationException("Event time cannot be before created at");
        }

        if (updatedAt != null && eventTime.isBefore(updatedAt)) {
            throw new PaymentValidationException("Event time cannot be before updated at");
        }
    }
}
