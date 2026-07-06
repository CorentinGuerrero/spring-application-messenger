package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import java.util.UUID;

public record OutboxMessageRecord(
    UUID messageId,
    String payload,
    int attempts
) {
}
