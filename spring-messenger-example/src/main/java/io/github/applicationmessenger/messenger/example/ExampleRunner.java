package io.github.applicationmessenger.messenger.example;

import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.QueryBus;
import io.github.applicationmessenger.messenger.example.application.command.RebuildUserSearchIndex;
import io.github.applicationmessenger.messenger.example.application.command.RegisterUser;
import io.github.applicationmessenger.messenger.example.application.command.SendWelcomeEmail;
import io.github.applicationmessenger.messenger.example.application.query.GetUser;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import io.github.applicationmessenger.messenger.example.domain.model.UserView;
import io.github.applicationmessenger.messenger.example.infrastructure.persistence.UserRepository;
import io.github.applicationmessenger.messenger.transport.jdbcoutbox.JdbcOutboxPublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "example", name = "run-demo", havingValue = "true", matchIfMissing = true)
public class ExampleRunner implements CommandLineRunner {
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final UserRepository userRepository;
    private final JdbcOutboxPublisher outboxPublisher;
    private final JdbcTemplate jdbcTemplate;

    public ExampleRunner(CommandBus commandBus, QueryBus queryBus, UserRepository userRepository, JdbcOutboxPublisher outboxPublisher, JdbcTemplate jdbcTemplate) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.userRepository = userRepository;
        this.outboxPublisher = outboxPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        UserId userId = commandBus.dispatch(new RegisterUser("john@example.com", "John"));
        UserView user = queryBus.ask(new GetUser(userId));

        System.out.println("Registered user: " + user);
        System.out.println("Users in database: " + userRepository.count());
        System.out.println("Messages waiting in outbox before publisher: " + outboxCount("PENDING"));
        System.out.println("Outbox messages published: " + outboxPublisher.publishOnce());
        System.out.println("Messages waiting in outbox after publisher: " + outboxCount("PENDING"));
        System.out.println("Messages published from outbox: " + outboxCount("PUBLISHED"));

        commandBus.dispatch(new SendWelcomeEmail(userId, user.email()));
        commandBus.dispatch(new RebuildUserSearchIndex(userId));
        System.out.println("Async commands sent to RabbitMQ and Kafka");
    }

    private int outboxCount(String status) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from messenger_outbox where status = ?", Integer.class, status);
        return count == null ? 0 : count;
    }
}
