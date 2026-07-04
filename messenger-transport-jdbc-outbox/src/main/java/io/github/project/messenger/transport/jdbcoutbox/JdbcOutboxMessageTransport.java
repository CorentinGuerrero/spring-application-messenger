package io.github.project.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.messenger.dispatch.BusType;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.exception.MessageDispatchException;
import io.github.project.messenger.exception.UnsupportedTransportOperationException;
import io.github.project.messenger.handler.MessageHandler;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.transport.MessageTransport;
import io.github.project.messenger.transport.TransportNames;
import io.github.project.messenger.transport.TransportMessage;

import java.sql.Timestamp;

public final class JdbcOutboxMessageTransport implements MessageTransport {
    public static final String TRANSPORT_NAME = TransportNames.JDBC_OUTBOX;
    public static final String PG_ALIAS = TransportNames.PG_OUTBOX_ALIAS;

    private final String name;
    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;
    private final JdbcOutboxTransportProperties properties;

    public JdbcOutboxMessageTransport(String name, OutboxMessageRepository repository, ObjectMapper objectMapper, JdbcOutboxTransportProperties properties) {
        this.name = name;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.properties = properties;
        validateTableName(properties.getTableName());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
        if (route.busType() == BusType.QUERY) {
            throw new UnsupportedTransportOperationException(name(), route.busType());
        }

        try {
            String payload = objectMapper.writeValueAsString(TransportMessage.from(envelope, route));
            repository.insert(insertSql(),
                envelope.metadata().messageId(),
                envelope.metadata().correlationId(),
                envelope.metadata().causationId(),
                route.busType().name(),
                route.messageType().getName(),
                route.transportName(),
                payload,
                properties.getInitialStatus(),
                Timestamp.from(envelope.metadata().createdAt())
            );
            return null;
        } catch (JsonProcessingException exception) {
            throw new MessageDispatchException("Could not serialize outbox message " + route.messageType().getName(), exception);
        }
    }

    private String insertSql() {
        return "insert into " + properties.getTableName()
            + " (message_id, correlation_id, causation_id, bus_type, message_type, transport_name, payload, status, created_at)"
            + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static void validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("Invalid outbox table name: " + tableName);
        }
    }
}
