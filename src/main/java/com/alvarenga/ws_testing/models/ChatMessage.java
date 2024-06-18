package com.alvarenga.ws_testing.models;

import com.alvarenga.ws_testing.documents.User;

public record ChatMessage(User from, User to, String message) {
}
