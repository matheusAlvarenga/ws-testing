package com.alvarenga.ws_testing.documents;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public record User(String id, String name, String picture) {
}
