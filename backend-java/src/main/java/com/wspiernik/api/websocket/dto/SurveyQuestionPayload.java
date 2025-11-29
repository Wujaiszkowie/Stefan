package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for survey_question messages.
 */
public record SurveyQuestionPayload(
        @JsonProperty("question") String question,
        @JsonProperty("step") int step
) {}
