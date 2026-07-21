package io.github.applicationmessenger.messenger.transport.kafka;

import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.Query;
import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.dispatch.EventErrorStrategy;
import io.github.applicationmessenger.messenger.dispatch.IncomingMessageDispatcher;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.envelope.MessageMetadata;
import io.github.applicationmessenger.messenger.exception.UnsupportedTransportOperationException;
import io.github.applicationmessenger.messenger.handler.InMemoryHandlerRegistry;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.TransportMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaMessageTransportTest {
    @Test
    void publishesEventToConfiguredTopic() {
        RecordingPublisher publisher = new RecordingPublisher();
        KafkaTransportProperties properties = new KafkaTransportProperties();
        properties.setTopics(Map.of(UserRegistered.class.getSimpleName(), "app.users"));
        KafkaMessageTransport transport = new KafkaMessageTransport(publisher, properties);
        MessageEnvelope envelope = MessageEnvelope.of(new UserRegistered("42"));

        Object result = transport.dispatch(envelope, new MessageRoute(BusType.EVENT, UserRegistered.class, "kafka"), next -> "local");

        assertThat(result).isNull();
        assertThat(publisher.topic).isEqualTo("app.users");
        assertThat(publisher.key).isEqualTo(envelope.metadata().messageId().toString());
        assertThat(publisher.message.payload()).isEqualTo(envelope.payload());
        assertThat(publisher.message.busType()).isEqualTo(BusType.EVENT);
    }

    @Test
    void usesDefaultTopicWhenNoneIsConfigured() {
        RecordingPublisher publisher = new RecordingPublisher();
        KafkaTransportProperties properties = new KafkaTransportProperties();
        properties.setTopicPrefix("platform");
        KafkaMessageTransport transport = new KafkaMessageTransport(publisher, properties);

        transport.dispatch(MessageEnvelope.of(new UserRegistered("42")), new MessageRoute(BusType.EVENT, UserRegistered.class, "kafka"), next -> null);

        assertThat(publisher.topic).isEqualTo("platform.event");
    }

    @Test
    void rejectsQueries() {
        KafkaMessageTransport transport = new KafkaMessageTransport(new RecordingPublisher(), new KafkaTransportProperties());

        assertThatThrownBy(() -> transport.dispatch(MessageEnvelope.of(new GetUser("42")), new MessageRoute(BusType.QUERY, GetUser.class, "kafka"), next -> null))
            .isInstanceOf(UnsupportedTransportOperationException.class)
            .hasMessageContaining("QUERY");
    }

    @Test
    void consumerDispatchesIncomingTransportMessageToLocalHandler() {
        AtomicReference<String> handledUserId = new AtomicReference<>();
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .eventHandler(UserRegistered.class, envelope -> {
                handledUserId.set(((UserRegistered) envelope.payload()).userId());
                return null;
            })
            .build();
        IncomingMessageDispatcher dispatcher = new IncomingMessageDispatcher(registry, List.of(), EventErrorStrategy.FAIL_FAST);
        KafkaTransportMessageConsumer consumer = new KafkaTransportMessageConsumer(new ObjectMapper().findAndRegisterModules(), dispatcher);
        TransportMessage message = new TransportMessage(
            UserRegistered.class.getName(),
            BusType.EVENT,
            "kafka",
            Map.of("userId", "42"),
            MessageMetadata.create()
        );

        consumer.consume(message);

        assertThat(handledUserId).hasValue("42");
    }

    record UserRegistered(String userId) implements Event {
    }

    record GetUser(String userId) implements Query<String> {
    }

    private static final class RecordingPublisher implements KafkaMessagePublisher {
        private String topic;
        private String key;
        private TransportMessage message;

        @Override
        public void publish(String topic, String key, TransportMessage message, MessageEnvelope envelope, MessageRoute route) {
            this.topic = topic;
            this.key = key;
            this.message = message;
        }
    }
}
