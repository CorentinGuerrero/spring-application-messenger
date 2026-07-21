package io.github.applicationmessenger.messenger.example.domain.event;

import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;

public record UserRegistered(UserId userId, String email) implements Event {
}
