package com.wspiernik.domain.conversation;

import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@ApplicationScoped
public class ConversationService {

    @Inject
    ConversationRepository conversationRepository;

    public long startNew(final String conversationType) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Conversation conversation = new Conversation();
            conversation.conversationType = conversationType;
            conversation.startedAt = LocalDateTime.now();
            conversationRepository.persist(conversation);
            return conversation.id;
        });
    }

    public void addMessage(final long conversationId, final LlmMessage llmMessage) {
        QuarkusTransaction.requiringNew().run(() -> {
            Conversation conversation = conversationRepository.findById(conversationId);
            conversation.addMessage(llmMessage);
            conversationRepository.persist(conversation);
        });
    }
}
