package io.github.corentinguerrero.messenger.example.domain.model;

import java.time.Instant;

public record UserView(UserId id, String email, String displayName, Instant createdAt) {
    public static UserView from(User user) {
        return new UserView(user.id(), user.email(), user.displayName(), user.createdAt());
    }
}
