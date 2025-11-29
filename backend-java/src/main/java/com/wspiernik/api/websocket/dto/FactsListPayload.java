package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Payload for facts_list query result messages.
 */
public record FactsListPayload(
        @JsonProperty("facts") List<FactDto> facts,
        @JsonProperty("total_count") long totalCount
) {}
