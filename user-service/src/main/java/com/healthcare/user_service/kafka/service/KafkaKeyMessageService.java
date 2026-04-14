package com.healthcare.user_service.kafka.service;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Getter
public class KafkaKeyMessageService {

    private final Set<UUID> keys = new HashSet<>();
}
