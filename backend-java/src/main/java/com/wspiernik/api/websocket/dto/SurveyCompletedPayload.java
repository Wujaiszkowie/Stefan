package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for survey_completed messages.
 */
public record SurveyCompletedPayload(
        @JsonProperty("profile_id") Long profileId,
        @JsonProperty("facts_saved") int factsSaved
) {}
