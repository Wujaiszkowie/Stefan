package com.wspiernik.domain.support;

import java.time.LocalDateTime;

/**
 * Holds the state of an ongoing support session.
 * Task 19: Support State - Session state for free-form conversations
 */
public class SupportState {

    private Long conversationId;
    private int messageCount;
    private LocalDateTime startedAt;
    private Integer stressLevel; // 1-10, assessed during conversation
    private String identifiedNeeds; // JSON array of identified needs
    private boolean completed;

    public SupportState() {
        this.messageCount = 0;
        this.startedAt = LocalDateTime.now();
        this.completed = false;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public Integer getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }

    public String getIdentifiedNeeds() {
        return identifiedNeeds;
    }

    public void setIdentifiedNeeds(String identifiedNeeds) {
        this.identifiedNeeds = identifiedNeeds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Check if conversation has reached a natural end point (5+ exchanges).
     */
    public boolean hasReachedNaturalEnd() {
        return messageCount >= 10; // 5 exchanges = 10 messages
    }

    /**
     * Check if it's time to offer summary (after 6+ exchanges).
     */
    public boolean shouldOfferSummary() {
        return messageCount >= 12;
    }
}
