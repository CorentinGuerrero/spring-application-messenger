package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.Event;
import io.github.corentinguerrero.messenger.Query;
import io.github.corentinguerrero.messenger.dispatch.BusType;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.exception.UnsupportedTransportOperationException;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcOutboxMessageTransportTest {
    @Test
    void insertsMessageIntoOutboxTable() {
        RecordingRepository repository = new RecordingRepository();
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        properties.setTableName("app_outbox");
        properties.setInitialStatus("NEW");
        JdbcOutboxMessageTransport transport = new JdbcOutboxMessageTransport(
            JdbcOutboxMessageTransport.TRANSPORT_NAME,
            repository,
            new ObjectMapper().findAndRegisterModules(),
            properties
        );
        MessageEnvelope envelope = MessageEnvelope.of(new UserRegistered("42"));

        Object result = transport.dispatch(envelope, new MessageRoute(BusType.EVENT, UserRegistered.class, "pg-outbox"), next -> "local");

        assertThat(result).isNull();
        assertThat(repository.sql).startsWith("insert into app_outbox");
        assertThat(repository.args).contains("EVENT", UserRegistered.class.getName(), "pg-outbox", "NEW");
        assertThat(Arrays.stream(repository.args).filter(String.class::isInstance).map(String.class::cast))
            .anyMatch(value -> value.contains("\"messageType\":\"" + UserRegistered.class.getName() + "\""));
    }

    @Test
    void rejectsUnsafeTableNames() {
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        properties.setTableName("messenger_outbox; drop table users");

        assertThatThrownBy(() -> new JdbcOutboxMessageTransport("pg-outbox", new RecordingRepository(), new ObjectMapper(), properties))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsQueries() {
        JdbcOutboxMessageTransport transport = new JdbcOutboxMessageTransport(
            JdbcOutboxMessageTransport.TRANSPORT_NAME,
            new RecordingRepository(),
            new ObjectMapper().findAndRegisterModules(),
            new JdbcOutboxTransportProperties()
        );

        assertThatThrownBy(() -> transport.dispatch(MessageEnvelope.of(new GetUser("42")), new MessageRoute(BusType.QUERY, GetUser.class, "pg-outbox"), next -> null))
            .isInstanceOf(UnsupportedTransportOperationException.class)
            .hasMessageContaining("QUERY");
    }

    record UserRegistered(String userId) implements Event {
    }

    record GetUser(String userId) implements Query<String> {
    }

    private static final class RecordingRepository implements OutboxMessageRepository {
        private String sql;
        private Object[] args;

        @Override
        public int insert(String sql, Object... args) {
            this.sql = sql;
            this.args = args;
            return 1;
        }

        @Override
        public void execute(String sql) {
            this.sql = sql;
        }
    }
}
