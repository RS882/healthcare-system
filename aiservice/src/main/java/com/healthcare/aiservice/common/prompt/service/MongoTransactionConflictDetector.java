package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.service.interfaces.TransactionConflictDetector;
import com.mongodb.MongoException;
import org.springframework.stereotype.Component;

@Component
public class MongoTransactionConflictDetector implements TransactionConflictDetector {

    private static final String TRANSIENT_TRANSACTION_ERROR =
            "TransientTransactionError";

    private static final int WRITE_CONFLICT_ERROR_CODE = 112;

    @Override
    public boolean isTransientTransactionConflict(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof MongoException mongoException
                    && isRetryableMongoException(mongoException)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private boolean isRetryableMongoException(
            MongoException exception
    ) {
        return exception.hasErrorLabel(
                TRANSIENT_TRANSACTION_ERROR
        ) || exception.getCode() == WRITE_CONFLICT_ERROR_CODE;
    }
}