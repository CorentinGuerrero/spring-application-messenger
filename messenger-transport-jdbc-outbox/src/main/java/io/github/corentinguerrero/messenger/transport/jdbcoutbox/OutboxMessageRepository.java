package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository {
    int insert(String sql, Object... args);

    void execute(String sql);

    default List<OutboxMessageRecord> findPending(String tableName, int batchSize) {
        return List.of();
    }

    default void markPublished(String tableName, UUID messageId, Instant publishedAt) {
    }

    default void markRetryable(String tableName, UUID messageId, int attempts, String error, Instant nextAttemptAt) {
    }

    default void markFailed(String tableName, UUID messageId, int attempts, String error) {
    }
}
