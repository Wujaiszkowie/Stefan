package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.MessageSender;
import com.wspiernik.api.websocket.dto.FactDto;
import com.wspiernik.api.websocket.dto.FactsListPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.domain.facts.Fact;
import com.wspiernik.domain.facts.FactRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Default implementation of QueryHandler.
 * Handles get_facts and get_profile queries.
 */
@ApplicationScoped
public class DefaultQueryHandler implements QueryHandler {

    private static final Logger LOG = Logger.getLogger(DefaultQueryHandler.class);

    @Inject
    MessageSender messageSender;

    @Inject
    FactRepository factRepository;

    @Override
    public void getFacts(WebSocketConnection connection, IncomingMessage message) {
        LOG.debugf("Getting facts, limit: %d", message.getLimit());

        int limit = message.getLimit();

        // Use QuarkusTransaction for database access in WebSocket context
        FactsListPayload payload = QuarkusTransaction.requiringNew().call(() -> {
            List<Fact> facts = factRepository.findAllFacts();
            long totalCount = factRepository.count();

            List<FactDto> factDtos = facts.stream()
                    .map(FactDto::from)
                    .toList();

            return new FactsListPayload(factDtos, totalCount);
        });

        messageSender.send(connection, OutgoingMessage.of(
                OutgoingMessage.FACTS_LIST, payload, message.requestId()));
    }
}
