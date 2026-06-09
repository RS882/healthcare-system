package com.healthcare.aiservice.repository;

import com.healthcare.aiservice.model.AiRequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiRequestLogRepository
        extends MongoRepository<AiRequestLog, String> {
}
