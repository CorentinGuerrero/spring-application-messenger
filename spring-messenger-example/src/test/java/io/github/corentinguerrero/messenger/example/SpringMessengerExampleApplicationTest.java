package io.github.corentinguerrero.messenger.example;

import io.github.corentinguerrero.messenger.CommandBus;
import io.github.corentinguerrero.messenger.QueryBus;
import io.github.corentinguerrero.messenger.example.application.command.RegisterUser;
import io.github.corentinguerrero.messenger.example.application.handler.SendWelcomeEmailHandler;
import io.github.corentinguerrero.messenger.example.application.query.GetUser;
import io.github.corentinguerrero.messenger.example.domain.event.UserRegistered;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;
import io.github.corentinguerrero.messenger.example.domain.model.UserView;
import io.github.corentinguerrero.messenger.example.infrastructure.persistence.UserRepository;
import io.github.corentinguerrero.messenger.transport.jdbcoutbox.JdbcOutboxPublisher;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "example.run-demo=false",
    "messenger.transports.rabbitmq.consumer.enabled=false",
    "messenger.transports.kafka.consumer.enabled=false"
})
class SpringMessengerExampleApplicationTest {
    @Autowired
    private CommandBus commandBus;

    @Autowired
    private QueryBus queryBus;

    @Autowired
    private SendWelcomeEmailHandler welcomeEmailHandler;

    @Autowired
    private JdbcOutboxPublisher outboxPublisher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void dispatchesCommandPersistsUserStoresEventInOutboxAndAnswersQuery() {
        UserId userId = commandBus.dispatch(new RegisterUser("jane@example.com", "Jane"));

        UserView user = queryBus.ask(new GetUser(userId));

        assertThat(user.email()).isEqualTo("jane@example.com");
        assertThat(user.displayName()).isEqualTo("Jane");
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(welcomeEmailHandler.sentEmails()).isEmpty();
        assertThat(jdbcTemplate.queryForObject("select count(*) from messenger_outbox", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select message_type from messenger_outbox", String.class))
            .isEqualTo(UserRegistered.class.getName());

        assertThat(outboxPublisher.publishOnce()).isEqualTo(1);
        assertThat(welcomeEmailHandler.sentEmails())
            .extracting(UserRegistered::userId)
            .contains(userId);
        assertThat(jdbcTemplate.queryForObject("select status from messenger_outbox", String.class)).isEqualTo("PUBLISHED");
    }

    @Test
    void validatesMessagesBeforeDispatching() {
        assertThatThrownBy(() -> commandBus.dispatch(new RegisterUser("not-an-email", "")))
            .isInstanceOf(ConstraintViolationException.class);
    }
}
