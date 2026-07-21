package io.github.applicationmessenger.messenger.transport.kafka;

import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.TransportMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class KafkaMessageTransportContainerTest {
    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Test
    void publishesMessageToKafkaTopic() {
        KafkaTransportProperties properties = new KafkaTransportProperties();
        properties.getTopics().put(UserRegistered.class.getSimpleName(), "messenger-user-events");
        KafkaMessageTransport transport = new KafkaMessageTransport(
            new DefaultKafkaMessagePublisher(kafkaTemplate()),
            properties
        );

        transport.dispatch(MessageEnvelope.of(new UserRegistered("42")), new MessageRoute(BusType.EVENT, UserRegistered.class, "kafka"), next -> null);

        try (KafkaConsumer<String, String> consumer = consumer()) {
            consumer.subscribe(List.of("messenger-user-events"));
            var records = consumer.poll(Duration.ofSeconds(10));

            assertThat(records).hasSize(1);
            String message = records.iterator().next().value();
            assertThat(message).contains("\"messageType\":\"" + UserRegistered.class.getName() + "\"");
            assertThat(message).contains("\"busType\":\"EVENT\"");
        }
    }

    private static KafkaTemplate<String, Object> kafkaTemplate() {
        Map<String, Object> properties = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
            properties,
            new StringSerializer(),
            new JsonSerializer<>(new ObjectMapper().findAndRegisterModules())
        ));
    }

    private static KafkaConsumer<String, String> consumer() {
        Map<String, Object> properties = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "messenger-test-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        return new KafkaConsumer<>(properties, new StringDeserializer(), new StringDeserializer());
    }

    record UserRegistered(String userId) implements Event {
    }
}
