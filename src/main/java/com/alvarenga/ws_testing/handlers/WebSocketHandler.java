package com.alvarenga.ws_testing.handlers;

import com.alvarenga.ws_testing.documents.User;
import com.alvarenga.ws_testing.models.ChatMessage;
import com.alvarenga.ws_testing.models.Event;
import com.alvarenga.ws_testing.models.EventType;
import com.alvarenga.ws_testing.models.MessagePayload;
import com.alvarenga.ws_testing.services.PublisherService;
import com.alvarenga.ws_testing.services.TicketService;
import com.alvarenga.ws_testing.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    private final TicketService ticketService;
    private final UserService userService;
    private final PublisherService publisherService;

    private final Map<String, WebSocketSession> sessions;
    private final Map<String, String> userIds;

    public WebSocketHandler(
            TicketService ticketService,
            UserService userService,
            PublisherService publisherService
    ) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.publisherService = publisherService;
        sessions = new ConcurrentHashMap<>();
        userIds = new ConcurrentHashMap<>();
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
        userIds.put(session.getId(), userId.get());

        logger.info("[WebSocketHandler.afterConnectionEstablished] User connected: " + userId.get());

        sendChatUsers(session);
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
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("[WebSocketHandler.handleMessage] Message received: " + message.getPayload());

        if (message.getPayload().equals("ping")) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        MessagePayload payload = new ObjectMapper().readValue(message.getPayload(), MessagePayload.class);

        String userIdFrom = userIds.get(session.getId());

        publisherService.publishChatMessage(userIdFrom, payload.to(), payload.message());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("[WebSocketHandler.afterConnectionClosed] Connection closed with session: " + session.getId());

        String userId = userIds.get(session.getId());

        if (userId != null) {
            sessions.remove(userId);
            userIds.remove(session.getId());
        }
    }

    private void sendChatUsers(WebSocketSession session) {
        List<User> chatUsers = userService.listAllUsers();

        Event<List<User>> event = new Event<>(EventType.CHAT_USERS_WERE_UPDATED, chatUsers);

        sendEvent(session, event);
    }

    private void sendEvent(WebSocketSession session, Event<?> event) {
        try {
            String eventSerialized = new ObjectMapper().writeValueAsString(event);
            session.sendMessage(new TextMessage(eventSerialized));
        } catch (Exception e) {
            logger.warning("[WebSocketHandler.sendEvent] Error sending event: " + e.getMessage());
        }
    }

    public void notify(ChatMessage chatMessage) {
        Event<ChatMessage> event = new Event<>(EventType.CHAT_MESSAGE_WAS_CREATED, chatMessage);

        List<String> users = List.of(chatMessage.from().id(), chatMessage.to().id());

        users.stream()
                .distinct()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .forEach(session -> sendEvent(session, event));

        logger.info("[WebSocketHandler.notify] Message sent to users: " + userIds);
    }
}
