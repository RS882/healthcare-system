package com.healthcare.aiservice.common.prompt.service.interfaces;

public interface TransactionConflictDetector {

    boolean isTransientTransactionConflict(Throwable throwable);
}
