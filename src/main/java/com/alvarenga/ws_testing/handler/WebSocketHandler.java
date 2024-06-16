package com.alvarenga.ws_testing.handler;

import com.alvarenga.ws_testing.services.TicketService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final static Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    private final TicketService ticketService;

    private final Map<String, WebSocketSession> sessions;

    public WebSocketHandler(TicketService ticketService) {
        this.ticketService = ticketService;
        sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("[WebSocketHandler.afterConnectionEstablished] Connection established with session: " + session.getId());

        Optional<String> ticket = ticketOf(session);

        if (ticket.isEmpty() || ticket.get().isBlank()) {
            logger.warning("[WebSocketHandler.afterConnectionEstablished] Ticket not found in session: " + session.getId());
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Optional<String> userId = ticketService.getUserIdByTicket(ticket.get());

        if (userId.isEmpty()) {
            logger.warning("[WebSocketHandler.afterConnectionEstablished] User not found for ticket: " + ticket.get());
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        sessions.put(userId.get(), session);

        logger.info("[WebSocketHandler.afterConnectionEstablished] User connected: " + userId.get());
    }

    private Optional<String> ticketOf(WebSocketSession session) {
        return Optional
                .ofNullable(session.getUri())
                .map(UriComponentsBuilder::fromUri)
                .map(UriComponentsBuilder::build)
                .map(UriComponents::getQueryParams)
                .map(it -> it.get("ticket"))
                .flatMap(it -> it.stream().findFirst())
                .map(String::trim);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        logger.info("[WebSocketHandler.handleMessage] Message received: " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("[WebSocketHandler.afterConnectionClosed] Connection closed with session: " + session.getId());
    }
}
