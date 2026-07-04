package io.github.project.messenger.transport;

import io.github.project.messenger.api.Spi;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.handler.MessageHandler;
import io.github.project.messenger.routing.MessageRoute;

@Spi
public interface MessageTransport {
    String name();

    Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next);
}
