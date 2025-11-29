package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wspiernik.infrastructure.persistence.entity.CaregiverProfile;

import java.time.LocalDateTime;

/**
 * Payload for profile_data messages.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileDataPayload(
        @JsonProperty("profile_id") Long profileId,
        @JsonProperty("caregiver_id") String caregiverId,
        @JsonProperty("ward_age") Integer wardAge,
        @JsonProperty("ward_conditions") String wardConditions,
        @JsonProperty("ward_medications") String wardMedications,
        @JsonProperty("ward_mobility_limits") String wardMobilityLimits,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
    /**
     * Create ProfileDataPayload from CaregiverProfile entity.
     */
    public static ProfileDataPayload from(CaregiverProfile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileDataPayload(
                profile.id,
                profile.caregiverId,
                profile.wardAge,
                profile.wardConditions,
                profile.wardMedications,
                profile.wardMobilityLimits,
                profile.createdAt,
                profile.updatedAt
        );
    }
}
