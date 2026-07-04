package io.github.project.messenger.transport.jdbcoutbox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcOutboxSchemaInitializerTest {
    @Test
    void createsTableAndIndexWhenEnabled() {
        RecordingRepository repository = new RecordingRepository();
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        properties.setTableName("app.messenger_outbox");

        new JdbcOutboxSchemaInitializer(repository, properties).afterPropertiesSet();

        assertThat(repository.executedSql).hasSize(6);
        assertThat(repository.executedSql.get(0)).contains("create table if not exists app.messenger_outbox");
        assertThat(repository.executedSql.get(0)).contains("message_id uuid primary key");
        assertThat(repository.executedSql.get(1)).contains("add column if not exists attempts");
        assertThat(repository.executedSql.get(5)).contains("create index if not exists app_messenger_outbox_idx");
        assertThat(repository.executedSql.get(5)).contains("on app.messenger_outbox (status, next_attempt_at, created_at)");
    }

    @Test
    void doesNothingWhenSchemaInitializationIsDisabled() {
        RecordingRepository repository = new RecordingRepository();
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        properties.setInitializeSchema(false);

        new JdbcOutboxSchemaInitializer(repository, properties).afterPropertiesSet();

        assertThat(repository.executedSql).isEmpty();
    }

    @Test
    void rejectsUnsafeTableNames() {
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        properties.setTableName("messenger_outbox;drop table users");

        assertThatThrownBy(() -> new JdbcOutboxSchemaInitializer(new RecordingRepository(), properties))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class RecordingRepository implements OutboxMessageRepository {
        private final List<String> executedSql = new ArrayList<>();

        @Override
        public int insert(String sql, Object... args) {
            return 1;
        }

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
        }
    }
}
