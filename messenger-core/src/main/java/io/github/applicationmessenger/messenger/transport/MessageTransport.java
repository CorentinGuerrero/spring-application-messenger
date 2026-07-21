package io.github.applicationmessenger.messenger.transport;

import io.github.applicationmessenger.messenger.api.Spi;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.routing.MessageRoute;

@Spi
public interface MessageTransport {
    String name();

    Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next);
}
