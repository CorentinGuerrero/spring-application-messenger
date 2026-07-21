package io.github.corentinguerrero.messenger.example.application.command;

import io.github.corentinguerrero.messenger.Command;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;

public record RebuildUserSearchIndex(UserId userId) implements Command<Void> {
}
