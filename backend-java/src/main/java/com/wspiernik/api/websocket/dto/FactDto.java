package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wspiernik.domain.facts.Fact;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for fact data in WebSocket messages.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FactDto(
        @JsonProperty("id") Long id,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("value") String value,
        @JsonProperty("severity") Integer severity,
        @JsonProperty("extracted_at") LocalDateTime extractedAt
) {
    /**
     * Create FactDto from Fact entity.
     */
    public static FactDto from(Fact fact) {
        return new FactDto(
                fact.id,
                fact.tags,
                fact.factValue,
                fact.severity,
                fact.extractedAt
        );
    }
}
