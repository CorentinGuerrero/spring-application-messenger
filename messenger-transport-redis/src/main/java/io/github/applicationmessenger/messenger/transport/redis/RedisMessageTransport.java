package io.github.applicationmessenger.messenger.transport.redis;

import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.exception.UnsupportedTransportOperationException;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.MessageTransport;
import io.github.applicationmessenger.messenger.transport.TransportNames;
import io.github.applicationmessenger.messenger.transport.TransportMessage;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class RedisMessageTransport implements MessageTransport {
    public static final String TRANSPORT_NAME = TransportNames.REDIS;

    private final RedisStreamMessagePublisher publisher;
    private final RedisTransportProperties properties;

    public RedisMessageTransport(RedisStreamMessagePublisher publisher, RedisTransportProperties properties) {
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

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", TransportMessage.from(envelope, route));
        body.put("messageId", envelope.metadata().messageId().toString());
        body.put("correlationId", envelope.metadata().correlationId().toString());
        body.put("busType", route.busType().name());
        body.put("messageType", route.messageType().getName());

        publisher.publish(stream(route), body);
        return null;
    }

    private String stream(MessageRoute route) {
        String configured = properties.getStreams().get(route.messageType().getName());
        if (configured == null) {
            configured = properties.getStreams().get(route.messageType().getSimpleName());
        }
        if (configured != null) {
            return configured;
        }
        return properties.getStreamPrefix() + ":" + route.busType().name().toLowerCase(Locale.ROOT);
    }
}
