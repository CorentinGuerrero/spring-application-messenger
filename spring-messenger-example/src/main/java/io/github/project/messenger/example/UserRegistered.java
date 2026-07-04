package io.github.project.messenger.example;

import io.github.project.messenger.Event;

public record UserRegistered(UserId userId, String email) implements Event {
}
