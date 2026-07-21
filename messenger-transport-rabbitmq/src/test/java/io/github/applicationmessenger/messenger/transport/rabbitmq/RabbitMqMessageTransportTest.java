package io.github.applicationmessenger.messenger.transport.rabbitmq;

import io.github.applicationmessenger.messenger.Command;
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

class RabbitMqMessageTransportTest {
    @Test
    void publishesCommandToConfiguredRoutingKey() {
        RecordingPublisher publisher = new RecordingPublisher();
        RabbitMqTransportProperties properties = new RabbitMqTransportProperties();
        properties.setExchange("app.exchange");
        properties.setRoutingKeys(Map.of(SendWelcomeEmail.class.getName(), "mail.welcome"));
        RabbitMqMessageTransport transport = new RabbitMqMessageTransport(publisher, properties);
        MessageEnvelope envelope = MessageEnvelope.of(new SendWelcomeEmail("42"));

        Object result = transport.dispatch(envelope, new MessageRoute(BusType.COMMAND, SendWelcomeEmail.class, "rabbitmq"), next -> "local");

        assertThat(result).isNull();
        assertThat(publisher.exchange).isEqualTo("app.exchange");
        assertThat(publisher.routingKey).isEqualTo("mail.welcome");
        assertThat(publisher.message.payload()).isEqualTo(envelope.payload());
        assertThat(publisher.message.messageType()).isEqualTo(SendWelcomeEmail.class.getName());
        assertThat(publisher.message.busType()).isEqualTo(BusType.COMMAND);
    }

    @Test
    void usesDefaultRoutingKeyWhenNoneIsConfigured() {
        RecordingPublisher publisher = new RecordingPublisher();
        RabbitMqTransportProperties properties = new RabbitMqTransportProperties();
        properties.setRoutingKeyPrefix("app.");
        RabbitMqMessageTransport transport = new RabbitMqMessageTransport(publisher, properties);

        transport.dispatch(MessageEnvelope.of(new SendWelcomeEmail("42")), new MessageRoute(BusType.COMMAND, SendWelcomeEmail.class, "rabbitmq"), next -> null);

        assertThat(publisher.routingKey).isEqualTo("app.command." + SendWelcomeEmail.class.getName());
    }

    @Test
    void rejectsQueries() {
        RabbitMqMessageTransport transport = new RabbitMqMessageTransport(new RecordingPublisher(), new RabbitMqTransportProperties());

        assertThatThrownBy(() -> transport.dispatch(MessageEnvelope.of(new GetUser("42")), new MessageRoute(BusType.QUERY, GetUser.class, "rabbitmq"), next -> null))
            .isInstanceOf(UnsupportedTransportOperationException.class)
            .hasMessageContaining("QUERY");
    }

    @Test
    void consumerDispatchesIncomingTransportMessageToLocalHandler() {
        AtomicReference<String> handledUserId = new AtomicReference<>();
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .commandHandler(SendWelcomeEmail.class, envelope -> {
                handledUserId.set(((SendWelcomeEmail) envelope.payload()).userId());
                return null;
            })
            .build();
        IncomingMessageDispatcher dispatcher = new IncomingMessageDispatcher(registry, List.of(), EventErrorStrategy.FAIL_FAST);
        RabbitMqMessageConsumer consumer = new RabbitMqMessageConsumer(new ObjectMapper().findAndRegisterModules(), dispatcher);
        TransportMessage message = new TransportMessage(
            SendWelcomeEmail.class.getName(),
            BusType.COMMAND,
            "rabbitmq",
            Map.of("userId", "42"),
            MessageMetadata.create()
        );

        consumer.consume(message);

        assertThat(handledUserId).hasValue("42");
    }

    record SendWelcomeEmail(String userId) implements Command<Void> {
    }

    record GetUser(String userId) implements Query<String> {
    }

    private static final class RecordingPublisher implements RabbitMqMessagePublisher {
        private String exchange;
        private String routingKey;
        private TransportMessage message;

        @Override
        public void publish(String exchange, String routingKey, TransportMessage message, MessageEnvelope envelope, MessageRoute route) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
        }
    }
}
