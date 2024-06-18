package com.alvarenga.ws_testing.services;

import com.alvarenga.ws_testing.config.RedisConfig;
import com.alvarenga.ws_testing.documents.User;
import com.alvarenga.ws_testing.models.ChatMessage;
import com.alvarenga.ws_testing.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class PublisherService {

    private static final Logger logger = Logger.getLogger(PublisherService.class.getName());

    private final UserRepository userRepository;
    private final ReactiveStringRedisTemplate redisTemplate;

    public PublisherService(UserRepository userRepository, ReactiveStringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    public void publishChatMessage(String userIdFrom, String userIdTo, String text) throws JsonProcessingException {
        User from = userRepository.findById(userIdFrom).orElseThrow();
        User to = userRepository.findById(userIdTo).orElseThrow();

        ChatMessage chatMessage = new ChatMessage(from, to, text);
        String chatMessageSerialized = new ObjectMapper().writeValueAsString(chatMessage);

        redisTemplate
                .convertAndSend(RedisConfig.CHAT_CHANNEL, chatMessageSerialized)
                .subscribe();

        logger.info("[PublisherService.publishChatMessage] Message published: " + chatMessageSerialized);
    }
}
