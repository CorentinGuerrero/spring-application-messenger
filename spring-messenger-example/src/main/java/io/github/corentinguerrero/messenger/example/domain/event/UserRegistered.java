package io.github.corentinguerrero.messenger.example.domain.event;

import io.github.corentinguerrero.messenger.Event;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;

public record UserRegistered(UserId userId, String email) implements Event {
}
