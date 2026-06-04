package com.healthcare.aiservice.common.message_classification.controller.API;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_URL;

public final class MessageClassificationApiPaths {

    private MessageClassificationApiPaths() {
    }

    public static final String CLASSIFY_MESSAGE = "/classify-message";

    public static final String CLASSIFY_MESSAGE_URL = AI_BASIC_URL+ CLASSIFY_MESSAGE;

}
