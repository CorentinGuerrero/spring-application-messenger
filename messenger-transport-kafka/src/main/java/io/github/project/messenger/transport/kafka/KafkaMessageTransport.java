package io.github.project.messenger.transport.kafka;

import io.github.project.messenger.dispatch.BusType;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.exception.UnsupportedTransportOperationException;
import io.github.project.messenger.handler.MessageHandler;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.transport.MessageTransport;
import io.github.project.messenger.transport.TransportNames;
import io.github.project.messenger.transport.TransportMessage;

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
