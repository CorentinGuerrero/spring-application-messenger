package io.github.applicationmessenger.messenger.example;

import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.QueryBus;
import io.github.applicationmessenger.messenger.example.application.command.RegisterUser;
import io.github.applicationmessenger.messenger.example.application.handler.SendWelcomeEmailHandler;
import io.github.applicationmessenger.messenger.example.application.query.GetUser;
import io.github.applicationmessenger.messenger.example.domain.event.UserRegistered;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import io.github.applicationmessenger.messenger.example.domain.model.UserView;
import io.github.applicationmessenger.messenger.transport.jdbcoutbox.JdbcOutboxPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
    "example.run-demo=false",
    "messenger.transports.rabbitmq.consumer.enabled=false",
    "messenger.transports.kafka.consumer.enabled=false"
})
class SpringMessengerExamplePostgresContainerTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private QueryBus queryBus;

    @Autowired
    private JdbcOutboxPublisher outboxPublisher;

    @Autowired
    private SendWelcomeEmailHandler welcomeEmailHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsUserAndOutboxRowInPostgres() {
        UserId userId = commandBus.dispatch(new RegisterUser("db@example.com", "Database User"));

        UserView user = queryBus.ask(new GetUser(userId));

        assertThat(user.email()).isEqualTo("db@example.com");
        assertThat(jdbcTemplate.queryForObject("select count(*) from app_users", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from messenger_outbox", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select status from messenger_outbox", String.class)).isEqualTo("PENDING");

        assertThat(outboxPublisher.publishOnce()).isEqualTo(1);
        assertThat(welcomeEmailHandler.sentEmails())
            .extracting(UserRegistered::userId)
            .contains(userId);
        assertThat(jdbcTemplate.queryForObject("select status from messenger_outbox", String.class)).isEqualTo("PUBLISHED");
    }
}
