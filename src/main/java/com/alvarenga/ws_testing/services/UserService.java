package com.alvarenga.ws_testing.services;

import com.alvarenga.ws_testing.documents.User;
import com.alvarenga.ws_testing.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }
}
