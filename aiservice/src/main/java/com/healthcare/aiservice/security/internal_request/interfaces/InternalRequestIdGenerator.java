package com.healthcare.aiservice.security.internal_request.interfaces;


import java.util.UUID;

@FunctionalInterface
public interface InternalRequestIdGenerator {

    UUID generate();
}