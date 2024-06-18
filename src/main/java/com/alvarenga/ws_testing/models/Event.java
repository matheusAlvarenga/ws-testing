package com.alvarenga.ws_testing.models;

public record Event<T>(EventType type, T payload) {
}
