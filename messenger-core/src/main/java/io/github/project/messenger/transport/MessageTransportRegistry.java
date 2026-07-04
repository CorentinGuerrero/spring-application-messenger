package io.github.project.messenger.transport;

import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.exception.NoTransportFoundException;
import io.github.project.messenger.handler.MessageHandler;
import io.github.project.messenger.routing.MessageRoute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MessageTransportRegistry {
    private final Map<String, MessageTransport> transports;

    public MessageTransportRegistry(List<MessageTransport> transports) {
        Map<String, MessageTransport> indexedTransports = new LinkedHashMap<>();
        for (MessageTransport transport : transports) {
            indexedTransports.put(normalize(transport.name()), transport);
        }
        this.transports = Map.copyOf(indexedTransports);
    }

    public static MessageTransportRegistry syncOnly() {
        return new MessageTransportRegistry(List.of(new InProcessMessageTransport()));
    }

    public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
        MessageTransport transport = transports.get(normalize(route.transportName()));
        if (transport == null) {
            throw new NoTransportFoundException(route.transportName());
        }
        return transport.dispatch(envelope, route, next);
    }

    private static String normalize(String transportName) {
        return transportName.toLowerCase(Locale.ROOT).trim();
    }
}
