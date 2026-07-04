package io.github.project.messenger.transport;

import io.github.project.messenger.api.Spi;
import io.github.project.messenger.dispatch.BusType;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.envelope.MessageMetadata;
import io.github.project.messenger.routing.MessageRoute;

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
