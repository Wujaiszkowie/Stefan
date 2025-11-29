package com.wspiernik.api.websocket;

import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages conversation sessions per WebSocket connection.
 * Tracks active conversation state (survey, intervention, support).
 */
@ApplicationScoped
public class ConversationSessionManager {

    private static final Logger LOG = Logger.getLogger(ConversationSessionManager.class);

    /**
     * Active sessions by connection ID.
     */
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();

    /**
     * Session data for a conversation.
     */
    public static class ConversationSession {
        public String sessionId;
        public String caregiverId;
        public String conversationType; // "survey", "intervention", "support"
        public Long conversationId;
        public int currentStep;
        public List<LlmMessage> messageHistory;
        public Map<String, Object> context;

        public ConversationSession(String sessionId, String conversationType) {
            this.sessionId = sessionId;
            this.conversationType = conversationType;
            this.currentStep = 0;
            this.messageHistory = new ArrayList<>();
            this.context = new HashMap<>();
        }

        /**
         * Add a message to the history.
         */
        public void addMessage(String role, String content) {
            messageHistory.add(new LlmMessage(role, content));
        }

        /**
         * Get context value.
         */
        @SuppressWarnings("unchecked")
        public <T> T getContextValue(String key) {
            return (T) context.get(key);
        }

        /**
         * Set context value.
         */
        public void setContextValue(String key, Object value) {
            context.put(key, value);
        }
    }

    /**
     * Start a new session for a connection.
     */
    public ConversationSession startSession(WebSocketConnection connection, String conversationType) {
        String connectionId = connection.id();

        if (sessions.containsKey(connectionId)) {
            LOG.warnf("Session already exists for %s, ending previous session", connectionId);
            endSession(connection);
        }

        ConversationSession session = new ConversationSession(connectionId, conversationType);
        sessions.put(connectionId, session);
        LOG.infof("Started %s session for %s", conversationType, connectionId);
        return session;
    }

    /**
     * Get session for a connection.
     */
    public ConversationSession getSession(WebSocketConnection connection) {
        return sessions.get(connection.id());
    }

    /**
     * Update session for a connection.
     */
    public void updateSession(WebSocketConnection connection, Consumer<ConversationSession> updater) {
        ConversationSession session = sessions.get(connection.id());
        if (session != null) {
            updater.accept(session);
        }
    }

    /**
     * End session for a connection.
     */
    public void endSession(WebSocketConnection connection) {
        ConversationSession removed = sessions.remove(connection.id());
        if (removed != null) {
            LOG.infof("Ended %s session for %s", removed.conversationType, connection.id());
        }
    }

    /**
     * Check if connection has an active session.
     */
    public boolean hasActiveSession(WebSocketConnection connection) {
        return sessions.containsKey(connection.id());
    }

    /**
     * Check if connection has an active session of a specific type.
     */
    public boolean hasActiveSession(WebSocketConnection connection, String conversationType) {
        ConversationSession session = sessions.get(connection.id());
        return session != null && conversationType.equals(session.conversationType);
    }

    /**
     * Get the number of active sessions.
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
