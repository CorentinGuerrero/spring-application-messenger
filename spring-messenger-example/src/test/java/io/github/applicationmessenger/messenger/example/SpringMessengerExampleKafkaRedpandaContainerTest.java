package io.github.applicationmessenger.messenger.example;

import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.example.application.command.RebuildUserSearchIndex;
import io.github.applicationmessenger.messenger.example.application.handler.RebuildUserSearchIndexHandler;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
    properties = {
        "example.run-demo=false",
        "messenger.transports.rabbitmq.consumer.enabled=false",
        "messenger.transports.kafka.consumer.enabled=true",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "spring.kafka.consumer.properties.spring.json.value.default.type=io.github.applicationmessenger.messenger.transport.TransportMessage",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
    }
)
class SpringMessengerExampleKafkaRedpandaContainerTest {
    @Container
    static final RedpandaContainer REDPANDA = new RedpandaContainer(
        DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v24.1.2")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", REDPANDA::getBootstrapServers);
    }

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private RebuildUserSearchIndexHandler searchIndexHandler;

    @Test
    void routesCommandToRedpandaAndConsumerInvokesApplicationHandler() {
        UserId userId = UserId.newId();

        Void result = commandBus.dispatch(new RebuildUserSearchIndex(userId));

        assertThat(result).isNull();
        await().atMost(Duration.ofSeconds(20))
            .untilAsserted(() -> assertThat(searchIndexHandler.rebuiltUsers()).contains(userId));
    }
}
