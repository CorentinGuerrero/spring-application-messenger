package io.github.project.messenger.transport.rabbitmq;

import io.github.project.messenger.dispatch.BusType;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.exception.UnsupportedTransportOperationException;
import io.github.project.messenger.handler.MessageHandler;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.transport.MessageTransport;
import io.github.project.messenger.transport.TransportNames;
import io.github.project.messenger.transport.TransportMessage;

import java.util.Locale;

public final class RabbitMqMessageTransport implements MessageTransport {
    public static final String TRANSPORT_NAME = TransportNames.RABBITMQ;

    private final RabbitMqMessagePublisher publisher;
    private final RabbitMqTransportProperties properties;

    public RabbitMqMessageTransport(RabbitMqMessagePublisher publisher, RabbitMqTransportProperties properties) {
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
            properties.getExchange(),
            routingKey(route),
            TransportMessage.from(envelope, route),
            envelope,
            route
        );
        return null;
    }

    private String routingKey(MessageRoute route) {
        String configured = properties.getRoutingKeys().get(route.messageType().getName());
        if (configured == null) {
            configured = properties.getRoutingKeys().get(route.messageType().getSimpleName());
        }
        if (configured != null) {
            return configured;
        }
        return properties.getRoutingKeyPrefix()
            + route.busType().name().toLowerCase(Locale.ROOT)
            + "."
            + route.messageType().getName();
    }

}
