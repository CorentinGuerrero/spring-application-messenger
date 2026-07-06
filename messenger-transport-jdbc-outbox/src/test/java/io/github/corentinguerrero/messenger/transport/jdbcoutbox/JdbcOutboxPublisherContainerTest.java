package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.Event;
import io.github.corentinguerrero.messenger.dispatch.BusType;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.MessageHandler;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import io.github.corentinguerrero.messenger.transport.MessageTransportRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class JdbcOutboxPublisherContainerTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Test
    void publishesPendingOutboxRowsToConfiguredTransport() {
        DataSource dataSource = dataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        DefaultOutboxMessageRepository repository = new DefaultOutboxMessageRepository(jdbcTemplate);
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        properties.getPublisher().setTargetTransport("capture");
        new JdbcOutboxSchemaInitializer(repository, properties).afterPropertiesSet();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        JdbcOutboxMessageTransport outboxTransport = new JdbcOutboxMessageTransport(
            JdbcOutboxMessageTransport.TRANSPORT_NAME,
            repository,
            objectMapper,
            properties
        );
        CaptureTransport captureTransport = new CaptureTransport();

        outboxTransport.dispatch(MessageEnvelope.of(new UserRegistered("42")), new MessageRoute(BusType.EVENT, UserRegistered.class, "pg-outbox"), next -> null);
        JdbcOutboxPublisher publisher = new JdbcOutboxPublisher(
            repository,
            new MessageTransportRegistry(List.of(captureTransport)),
            objectMapper,
            properties,
            new TransactionTemplate(new DataSourceTransactionManager(dataSource))
        );

        int published = publisher.publishOnce();

        assertThat(published).isEqualTo(1);
        assertThat(captureTransport.payload).hasValue(new UserRegistered("42"));
        Map<String, Object> row = jdbcTemplate.queryForMap("select status, attempts, published_at from messenger_outbox");
        assertThat(row.get("status")).isEqualTo("PUBLISHED");
        assertThat(row.get("attempts")).isEqualTo(0);
        assertThat(row.get("published_at")).isNotNull();
    }

    private static DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(postgres.getDriverClassName());
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return dataSource;
    }

    record UserRegistered(String userId) implements Event {
    }

    static final class CaptureTransport implements MessageTransport {
        private final AtomicReference<Object> payload = new AtomicReference<>();

        @Override
        public String name() {
            return "capture";
        }

        @Override
        public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
            payload.set(envelope.payload());
            return null;
        }
    }
}
