package com.alvarenga.ws_testing.services;

import com.alvarenga.ws_testing.config.RedisConfig;
import com.alvarenga.ws_testing.handlers.WebSocketHandler;
import com.alvarenga.ws_testing.models.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Subscriber {

    private static final Logger logger = Logger.getLogger(Subscriber.class.getName());

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebSocketHandler webSocketHandler;

    public Subscriber(ReactiveStringRedisTemplate redisTemplate, WebSocketHandler webSocketHandler) {
        this.redisTemplate = redisTemplate;
        this.webSocketHandler = webSocketHandler;
    }

    @PostConstruct
    private void init() {
        this.redisTemplate.listenTo(ChannelTopic.of(RedisConfig.CHAT_CHANNEL))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(this::onChatMessage);
    }

    private void onChatMessage(final String chatMessageSerialized) {
        logger.info("[Subscriber.onChatMessage] Message received: " + chatMessageSerialized);
        try {
            ChatMessage chatMessage = new ObjectMapper().readValue(chatMessageSerialized, ChatMessage.class);

            webSocketHandler.notify(chatMessage);
        } catch (Exception e) {
            logger.warning("[Subscriber.onChatMessage] Error deserializing message: " + chatMessageSerialized);
        }
    }
}
