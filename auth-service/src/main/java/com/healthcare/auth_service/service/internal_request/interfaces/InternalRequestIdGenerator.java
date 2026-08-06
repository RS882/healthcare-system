package com.healthcare.auth_service.service.internal_request.interfaces;


import java.util.UUID;

@FunctionalInterface
public interface InternalRequestIdGenerator {

    UUID generate();
}