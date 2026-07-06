package io.github.corentinguerrero.messenger.transport;

import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.MessageHandler;
import io.github.corentinguerrero.messenger.routing.MessageRoute;

public final class InProcessMessageTransport implements MessageTransport {
    @Override
    public String name() {
        return TransportNames.SYNC;
    }

    @Override
    public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
        return next.handle(envelope);
    }
}
