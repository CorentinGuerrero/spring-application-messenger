package io.github.corentinguerrero.messenger.transport.kafka;

import io.github.corentinguerrero.messenger.dispatch.BusType;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.exception.UnsupportedTransportOperationException;
import io.github.corentinguerrero.messenger.handler.MessageHandler;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import io.github.corentinguerrero.messenger.transport.TransportNames;
import io.github.corentinguerrero.messenger.transport.TransportMessage;

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
