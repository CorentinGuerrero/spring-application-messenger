package io.github.applicationmessenger.messenger.transport;

import io.github.applicationmessenger.messenger.api.Spi;
import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.envelope.MessageMetadata;
import io.github.applicationmessenger.messenger.routing.MessageRoute;

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
