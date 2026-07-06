package io.github.corentinguerrero.messenger.transport;

import io.github.corentinguerrero.messenger.api.Spi;
import io.github.corentinguerrero.messenger.dispatch.BusType;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.envelope.MessageMetadata;
import io.github.corentinguerrero.messenger.routing.MessageRoute;

@Spi
public record TransportMessage(
    String messageType,
    BusType busType,
    String transportName,
    Object payload,
    MessageMetadata metadata
) {
    public static TransportMessage from(MessageEnvelope envelope, MessageRoute route) {
        return new TransportMessage(
            route.messageType().getName(),
            route.busType(),
            route.transportName(),
            envelope.payload(),
            envelope.metadata()
        );
    }
}
