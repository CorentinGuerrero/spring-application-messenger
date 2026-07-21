package io.github.applicationmessenger.messenger.example.application.command;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;

public record RebuildUserSearchIndex(UserId userId) implements Command<Void> {
}
