package com.wspiernik.domain.facts;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing an extracted fact from a conversation.
 * Facts are structured pieces of health information (symptoms, medications, events, etc.).
 */
@Entity
@Table(name = "facts")
public class Fact extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "conversation_id")
    public Long conversationId;

    @Column(name = "fact_type")
    public List<String> tags;

    @Column(name = "fact_value", columnDefinition = "TEXT")
    public String factValue;

    @Column(name = "severity")
    public Integer severity; // 1-10 (nullable)

    @Column(name = "extracted_at")
    public LocalDateTime extractedAt;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}
