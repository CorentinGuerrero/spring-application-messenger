package io.github.corentinguerrero.messenger.transport.redis;

import io.github.corentinguerrero.messenger.Command;
import io.github.corentinguerrero.messenger.Query;
import io.github.corentinguerrero.messenger.dispatch.BusType;
import io.github.corentinguerrero.messenger.dispatch.EventErrorStrategy;
import io.github.corentinguerrero.messenger.dispatch.IncomingMessageDispatcher;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.envelope.MessageMetadata;
import io.github.corentinguerrero.messenger.exception.UnsupportedTransportOperationException;
import io.github.corentinguerrero.messenger.handler.InMemoryHandlerRegistry;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.transport.TransportMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisMessageTransportTest {
    @Test
    void appendsMessageToConfiguredStream() {
        RecordingPublisher publisher = new RecordingPublisher();
        RedisTransportProperties properties = new RedisTransportProperties();
        properties.setStreams(Map.of(RebuildSearchIndex.class.getName(), "jobs:search"));
        RedisMessageTransport transport = new RedisMessageTransport(publisher, properties);
        MessageEnvelope envelope = MessageEnvelope.of(new RebuildSearchIndex("users"));

        Object result = transport.dispatch(envelope, new MessageRoute(BusType.COMMAND, RebuildSearchIndex.class, "redis"), next -> "local");

        assertThat(result).isNull();
        assertThat(publisher.stream).isEqualTo("jobs:search");
        assertThat(publisher.body.get("messageId")).isEqualTo(envelope.metadata().messageId().toString());
        assertThat(publisher.body.get("busType")).isEqualTo("COMMAND");
        assertThat(publisher.body.get("messageType")).isEqualTo(RebuildSearchIndex.class.getName());
        assertThat((TransportMessage) publisher.body.get("message")).extracting(TransportMessage::payload).isEqualTo(envelope.payload());
    }

    @Test
    void usesDefaultStreamWhenNoneIsConfigured() {
        RecordingPublisher publisher = new RecordingPublisher();
        RedisTransportProperties properties = new RedisTransportProperties();
        properties.setStreamPrefix("app");
        RedisMessageTransport transport = new RedisMessageTransport(publisher, properties);

        transport.dispatch(MessageEnvelope.of(new RebuildSearchIndex("users")), new MessageRoute(BusType.COMMAND, RebuildSearchIndex.class, "redis"), next -> null);

        assertThat(publisher.stream).isEqualTo("app:command");
    }

    @Test
    void rejectsQueries() {
        RedisMessageTransport transport = new RedisMessageTransport(new RecordingPublisher(), new RedisTransportProperties());

        assertThatThrownBy(() -> transport.dispatch(MessageEnvelope.of(new GetUser("42")), new MessageRoute(BusType.QUERY, GetUser.class, "redis"), next -> null))
            .isInstanceOf(UnsupportedTransportOperationException.class)
            .hasMessageContaining("QUERY");
    }

    @Test
    void consumerDispatchesIncomingTransportMessageToLocalHandler() {
        AtomicReference<String> handledIndex = new AtomicReference<>();
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .commandHandler(RebuildSearchIndex.class, envelope -> {
                handledIndex.set(((RebuildSearchIndex) envelope.payload()).indexName());
                return null;
            })
            .build();
        IncomingMessageDispatcher dispatcher = new IncomingMessageDispatcher(registry, List.of(), EventErrorStrategy.FAIL_FAST);
        RedisTransportMessageConsumer consumer = new RedisTransportMessageConsumer(new ObjectMapper().findAndRegisterModules(), dispatcher);
        TransportMessage message = new TransportMessage(
            RebuildSearchIndex.class.getName(),
            BusType.COMMAND,
            "redis",
            Map.of("indexName", "users"),
            MessageMetadata.create()
        );

        consumer.consume(message);

        assertThat(handledIndex).hasValue("users");
    }

    record RebuildSearchIndex(String indexName) implements Command<Void> {
    }

    record GetUser(String userId) implements Query<String> {
    }

    private static final class RecordingPublisher implements RedisStreamMessagePublisher {
        private String stream;
        private Map<String, Object> body;

        @Override
        public void publish(String stream, Map<String, Object> body) {
            this.stream = stream;
            this.body = body;
        }
    }
}
