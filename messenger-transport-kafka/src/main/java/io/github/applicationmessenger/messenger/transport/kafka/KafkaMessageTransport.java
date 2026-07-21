package io.github.applicationmessenger.messenger.transport.kafka;

import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.exception.UnsupportedTransportOperationException;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.MessageTransport;
import io.github.applicationmessenger.messenger.transport.TransportNames;
import io.github.applicationmessenger.messenger.transport.TransportMessage;

import java.util.Locale;

public final class KafkaMessageTransport implements MessageTransport {
    public static final String TRANSPORT_NAME = TransportNames.KAFKA;

    private final KafkaMessagePublisher publisher;
    private final KafkaTransportProperties properties;

    public KafkaMessageTransport(KafkaMessagePublisher publisher, KafkaTransportProperties properties) {
        this.publisher = publisher;
        this.properties = properties;
    }

    @Override
    public String name() {
        return TRANSPORT_NAME;
    }

    @Override
    public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
        if (route.busType() == BusType.QUERY) {
            throw new UnsupportedTransportOperationException(name(), route.busType());
        }

        publisher.publish(
            topic(route),
            envelope.metadata().messageId().toString(),
            TransportMessage.from(envelope, route),
            envelope,
            route
        );
        return null;
    }

    private String topic(MessageRoute route) {
        String configured = properties.getTopics().get(route.messageType().getName());
        if (configured == null) {
            configured = properties.getTopics().get(route.messageType().getSimpleName());
        }
        if (configured != null) {
            return configured;
        }
        return properties.getTopicPrefix() + "." + route.busType().name().toLowerCase(Locale.ROOT);
    }

}
