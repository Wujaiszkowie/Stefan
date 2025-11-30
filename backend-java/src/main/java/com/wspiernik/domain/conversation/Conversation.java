package com.wspiernik.domain.conversation;

import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import com.wspiernik.infrastructure.persistence.JsonToLlmMessageListConverter;
import com.wspiernik.infrastructure.persistence.JsonToStringListConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Entity representing a conversation session.
 * Stores the full transcript and metadata for survey, intervention, or support sessions.
 */
@Entity
@Table(name = "conversations")
public class Conversation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "conversation_type")
    public String conversationType; // "survey", "intervention", "support"

    @Column(name = "raw_transcript", columnDefinition = "TEXT")
    @Convert(converter = JsonToLlmMessageListConverter.class)
    public List<LlmMessage> rawTranscript;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "ended_at")
    public LocalDateTime endedAt;

    void addMessage(LlmMessage message) {
        rawTranscript.add(message);
    }

    public static Optional<Conversation> findById(Long id) {
        return find("id", id).firstResultOptional();
    }

    /**
     * Find conversations by type.
     */
    public static List<Conversation> findByConversationType(String type) {
        return list("conversationType", type);
    }

    /**
     * Find conversations that haven't been processed for facts extraction.
     */
    public static List<Conversation> findUnprocessedForFactsExtraction() {
        return list("factsExtracted", false);
    }
}
