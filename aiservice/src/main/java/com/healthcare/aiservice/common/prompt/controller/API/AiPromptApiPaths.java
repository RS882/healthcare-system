package com.healthcare.aiservice.common.prompt.controller.API;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_ADMIN_URL;

public final class AiPromptApiPaths {

    private AiPromptApiPaths() {
    }

    public static final String PATH_VARIABLE_PROMPT_ID = "promptId";

    /**
     * POST  /prompts
     * GET   /prompts
     */
    public static final String PROMPTS = "/prompts";

    /**
     * GET /prompts/{promptId}
     */
    public static final String PROMPT_BY_ID =
            "/prompts/{" + PATH_VARIABLE_PROMPT_ID + "}";

    /**
     * PATCH /prompts/{promptId}/activate
     */
    public static final String ACTIVATE_PROMPT =
            "/prompts/{" + PATH_VARIABLE_PROMPT_ID + "}/activate";

    /**
     * GET /prompts/current
     */
    public static final String CURRENT_PROMPT =
            "/prompts/current";

    // Full URLs

    public static final String PROMPTS_URL =
            AI_BASIC_ADMIN_URL + PROMPTS;

    public static final String PROMPT_BY_ID_URL =
            AI_BASIC_ADMIN_URL + PROMPT_BY_ID;

    public static final String ACTIVATE_PROMPT_URL =
            AI_BASIC_ADMIN_URL + ACTIVATE_PROMPT;

    public static final String CURRENT_PROMPT_URL =
            AI_BASIC_ADMIN_URL + CURRENT_PROMPT;
}