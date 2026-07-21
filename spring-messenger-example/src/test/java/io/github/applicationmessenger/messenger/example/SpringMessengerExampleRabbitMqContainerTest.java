package io.github.applicationmessenger.messenger.example;

import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.example.application.command.SendWelcomeEmail;
import io.github.applicationmessenger.messenger.example.application.handler.SendWelcomeEmailCommandHandler;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
@SpringBootTest(
    classes = SpringMessengerExampleApplication.class,
    properties = {
        "example.run-demo=false",
        "messenger.transports.rabbitmq.consumer.enabled=true",
        "messenger.transports.kafka.consumer.enabled=false"
    }
)
class SpringMessengerExampleRabbitMqContainerTest {
    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-alpine"));

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private SendWelcomeEmailCommandHandler welcomeEmailCommandHandler;

    @Test
    void routesCommandToRabbitMqAndConsumerInvokesApplicationHandler() {
        UserId userId = UserId.newId();
        SendWelcomeEmail command = new SendWelcomeEmail(userId, "async-rabbit@example.com");

        Void result = commandBus.dispatch(command);

        assertThat(result).isNull();
        await().atMost(Duration.ofSeconds(20))
            .untilAsserted(() -> assertThat(welcomeEmailCommandHandler.sentCommands()).contains(command));
    }
}
