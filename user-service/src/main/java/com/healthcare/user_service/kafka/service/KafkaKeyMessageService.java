package com.healthcare.user_service.kafka.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Data
public class KafkaKeyMessageService {
    private Set<UUID> keys = new HashSet<>();
}
