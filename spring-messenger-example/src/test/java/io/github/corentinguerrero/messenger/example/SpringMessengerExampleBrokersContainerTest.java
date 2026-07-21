package io.github.corentinguerrero.messenger.example;

import io.github.corentinguerrero.messenger.CommandBus;
import io.github.corentinguerrero.messenger.example.application.command.RebuildUserSearchIndex;
import io.github.corentinguerrero.messenger.example.application.command.SendWelcomeEmail;
import io.github.corentinguerrero.messenger.example.application.handler.RebuildUserSearchIndexHandler;
import io.github.corentinguerrero.messenger.example.application.handler.SendWelcomeEmailCommandHandler;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
@SpringBootTest(
    classes = SpringMessengerExampleApplication.class,
    properties = "example.run-demo=false"
)
class SpringMessengerExampleBrokersContainerTest {
    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-alpine"));

    @Container
    static final RedpandaContainer REDPANDA = new RedpandaContainer(
        DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v24.1.2")
    );

    @DynamicPropertySource
    static void brokerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        registry.add("spring.kafka.bootstrap-servers", REDPANDA::getBootstrapServers);
    }

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private SendWelcomeEmailCommandHandler welcomeEmailCommandHandler;

    @Autowired
    private RebuildUserSearchIndexHandler searchIndexHandler;

    @Test
    void globalConfigurationRoutesRabbitMqAndKafkaMessagesToApplicationHandlers() {
        UserId userId = UserId.newId();
        SendWelcomeEmail welcomeEmail = new SendWelcomeEmail(userId, "async-all@example.com");

        commandBus.dispatch(welcomeEmail);
        commandBus.dispatch(new RebuildUserSearchIndex(userId));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(welcomeEmailCommandHandler.sentCommands()).contains(welcomeEmail);
            assertThat(searchIndexHandler.rebuiltUsers()).contains(userId);
        });
    }
}
