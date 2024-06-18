package com.alvarenga.ws_testing.repositories;

import com.alvarenga.ws_testing.documents.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
