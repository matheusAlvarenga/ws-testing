package com.alvarenga.ws_testing.services;

import com.alvarenga.ws_testing.documents.User;
import com.alvarenga.ws_testing.providers.TokenProvider;
import com.alvarenga.ws_testing.repositories.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public TicketService(
            RedisTemplate<String, String> redisTemplate,
            TokenProvider tokenProvider,
            UserRepository userRepository
    ) {
        this.redisTemplate = redisTemplate;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public String buildAndSaveTicket(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        Map<String, String> user = tokenProvider.decode(token);
        String userId = user.get("id");

        String ticket = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(ticket, userId, Duration.ofSeconds(10L));

        saveUser(user);

        return ticket;
    }

    private void saveUser(Map<String, String> user) {
        userRepository.save(new User(user.get("id"), user.get("email"), user.get("picture")));
    }

    public Optional<String> getUserIdByTicket(String ticket) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(ticket));
    }
}
