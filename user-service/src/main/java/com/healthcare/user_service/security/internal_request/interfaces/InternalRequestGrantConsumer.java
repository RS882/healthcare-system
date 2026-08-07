package com.healthcare.user_service.security.internal_request.interfaces;



import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;

import java.util.UUID;

public interface InternalRequestGrantConsumer {

    InternalRequestGrant consume(UUID internalRequestId);
}
