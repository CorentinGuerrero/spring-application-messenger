package io.github.applicationmessenger.messenger.transport;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.routing.MessageRoute;

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
