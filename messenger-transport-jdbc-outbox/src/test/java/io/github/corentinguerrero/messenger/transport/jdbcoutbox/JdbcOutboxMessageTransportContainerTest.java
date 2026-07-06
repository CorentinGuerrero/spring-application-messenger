package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.Event;
import io.github.corentinguerrero.messenger.dispatch.BusType;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class JdbcOutboxMessageTransportContainerTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Test
    void insertsMessageIntoPostgresOutboxTable() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
        JdbcOutboxTransportProperties properties = new JdbcOutboxTransportProperties();
        new JdbcOutboxSchemaInitializer(new DefaultOutboxMessageRepository(jdbcTemplate), properties).afterPropertiesSet();
        JdbcOutboxMessageTransport transport = new JdbcOutboxMessageTransport(
            JdbcOutboxMessageTransport.TRANSPORT_NAME,
            new DefaultOutboxMessageRepository(jdbcTemplate),
            new ObjectMapper().findAndRegisterModules(),
            properties
        );

        transport.dispatch(MessageEnvelope.of(new UserRegistered("42")), new MessageRoute(BusType.EVENT, UserRegistered.class, "pg-outbox"), next -> null);

        Map<String, Object> row = jdbcTemplate.queryForMap("select bus_type, message_type, transport_name, status, payload from messenger_outbox");
        assertThat(row.get("bus_type")).isEqualTo("EVENT");
        assertThat(row.get("message_type")).isEqualTo(UserRegistered.class.getName());
        assertThat(row.get("transport_name")).isEqualTo("pg-outbox");
        assertThat(row.get("status")).isEqualTo("PENDING");
        assertThat((String) row.get("payload")).contains("\"messageType\":\"" + UserRegistered.class.getName() + "\"");
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
}
