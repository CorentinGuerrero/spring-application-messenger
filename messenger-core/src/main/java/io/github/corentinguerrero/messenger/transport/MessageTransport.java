package io.github.corentinguerrero.messenger.transport;

import io.github.corentinguerrero.messenger.api.Spi;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.MessageHandler;
import io.github.corentinguerrero.messenger.routing.MessageRoute;

@Spi
public interface MessageTransport {
    String name();

    Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next);
}
