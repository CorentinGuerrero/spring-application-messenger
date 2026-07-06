package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

final class DefaultOutboxMessageRepository implements OutboxMessageRepository {
    private final JdbcTemplate jdbcTemplate;

    DefaultOutboxMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int insert(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }

    @Override
    public List<OutboxMessageRecord> findPending(String tableName, int batchSize) {
        return jdbcTemplate.query("""
                select message_id, payload, attempts
                from %s
                where status = 'PENDING'
                  and (next_attempt_at is null or next_attempt_at <= current_timestamp)
                order by created_at
                limit ?
                for update skip locked
                """.formatted(tableName),
            (rs, rowNum) -> new OutboxMessageRecord(
                rs.getObject("message_id", UUID.class),
                rs.getString("payload"),
                rs.getInt("attempts")
            ),
            batchSize
        );
    }

    @Override
    public void markPublished(String tableName, UUID messageId, Instant publishedAt) {
        jdbcTemplate.update(
            "update " + tableName + " set status = 'PUBLISHED', published_at = ?, last_error = null where message_id = ?",
            Timestamp.from(publishedAt),
            messageId
        );
    }

    @Override
    public void markRetryable(String tableName, UUID messageId, int attempts, String error, Instant nextAttemptAt) {
        jdbcTemplate.update(
            "update " + tableName + " set attempts = ?, last_error = ?, next_attempt_at = ?, status = 'PENDING' where message_id = ?",
            attempts,
            truncate(error),
            Timestamp.from(nextAttemptAt),
            messageId
        );
    }

    @Override
    public void markFailed(String tableName, UUID messageId, int attempts, String error) {
        jdbcTemplate.update(
            "update " + tableName + " set attempts = ?, last_error = ?, status = 'FAILED' where message_id = ?",
            attempts,
            truncate(error),
            messageId
        );
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= 4000) {
            return value;
        }
        return value.substring(0, 4000);
    }
}
