package io.github.corentinguerrero.messenger.example.domain.model;

import java.time.Instant;

public record User(UserId id, String email, String displayName, Instant createdAt) {
    public static User register(String email, String displayName) {
        return new User(UserId.newId(), email, displayName, Instant.now());
    }
}
